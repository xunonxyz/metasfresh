// note that we set a default version for this library in jenkins, so we don't have to specify it here
@Library('misc')
import de.metas.jenkins.DockerConf
@Library('misc')
import de.metas.jenkins.DockerConf
import de.metas.jenkins.MvnConf

Map build(final MvnConf mvnConf,
          final Map scmVars,
          final boolean forceBuild = false,
          final boolean forceSkip = false,
          final String multithreadParam = "-T 2C") {

    final dockerImages = [:]
    final def misc = new de.metas.jenkins.Misc()

    stage('Build backend')
            {
                currentBuild.description = """${currentBuild.description}<p/>
					<h2>Backend</h2>
				"""
                if (forceSkip) {
                    currentBuild.description = """${currentBuild.description}<p/>
            Forced to skip.
            """
                    echo "forced to skip backend";
                    return;
                }

                def anyFileChanged
                try {
                    def vgitout = sh(returnStdout: true, script: "git diff --name-only ${scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT} ${scmVars.GIT_COMMIT} .").trim()
                    echo "git diff output (modified files):\n>>>>>\n${vgitout}\n<<<<<"
                    anyFileChanged = !vgitout.isEmpty()
                    // see if anything at all changed in this folder
                    echo "Any file changed compared to last build: ${anyFileChanged}"
                } catch (ignored) {
                    echo "git diff error => assume something must have changed"
                    anyFileChanged = true
                }

                if (scmVars.GIT_COMMIT && scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT && !anyFileChanged && !forceBuild) {
                    currentBuild.description = """${currentBuild.description}<p/>
					No changes happened in backend.
					"""
                    echo "no changes happened in backend; skip building backend";
                    return;
                }

                final String VERSIONS_PLUGIN = 'org.codehaus.mojo:versions-maven-plugin:2.8.1' // make sure we know which plugin version we run

                // set the root-pom's parent pom. Although the parent pom is available via relativePath, we need it to be this build's version then the root pom is deployed to our maven-repo
                sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DparentVersion=${env.MF_VERSION} ${mvnConf.resolveParams} ${VERSIONS_PLUGIN}:update-parent"

                // set the artifact version of everything below ${mvnConf.pomFile}
                // processAllModules=true: also update those modules that have a parent version range!
                sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DnewVersion=${env.MF_VERSION} -DprocessAllModules=true -Dincludes=\"de.metas*:*\" ${mvnConf.resolveParams} ${VERSIONS_PLUGIN}:set"
                // Set the metasfresh.version property from [1,10.0.0] to our current build version
                // From the documentation: "Set a property to a given version without any sanity checks"; that's what we want here..sanity is clearly overated
                sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -Dproperty=metasfresh.version -DnewVersion=${env.MF_VERSION} ${VERSIONS_PLUGIN}:set-property"

                final String metasfreshCommonUpdatePropertyParam = "-Dproperty=metasfresh-common.version -DallowDowngrade=true"
                sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode ${mvnConf.resolveParams} ${metasfreshCommonUpdatePropertyParam} ${VERSIONS_PLUGIN}:update-property"

                // build and deploy
                // GOAL: don't deploy - but we are not there yet
                // TODO: put all jaspers&SQLs into their respective Docker images within the backend-build. Then we only need to deploy a few selected individual files; see frontend's build.groovy for how to do that			// maven.test.failure.ignore=true: continue if tests fail, because we want a full report.
                // about -Dmetasfresh.assembly.descriptor.version: the versions plugin can't update the version of our shared assembly descriptor de.metas.assemblies. Therefore we need to provide the version from outside via this property
                // about -T 2C: it means "run with 2 threads per CPU"; note that for us this is highly experimental
                sh "mvn --settings ${mvnConf.settingsFile} ${multithreadParam} --file ${mvnConf.pomFile} --batch-mode -Dmaven.test.failure.ignore=true -Dmetasfresh.assembly.descriptor.version=${env.MF_VERSION} ${mvnConf.resolveParams} ${mvnConf.deployParam} clean deploy"

                final DockerConf reportDockerConf = new DockerConf(
                        'metasfresh-report', // artifactName
                        env.BRANCH_NAME, // branchName
                        env.MF_VERSION, // versionSuffix
                        'de.metas.report/metasfresh-report-service-standalone/target/docker' // workDir
                );
                dockerImages['report'] = dockerBuildAndPush(reportDockerConf)

                final DockerConf msv3ServerDockerConf = reportDockerConf
                        .withArtifactName('de.metas.vertical.pharma.msv3.server')
                        .withWorkDir('de.metas.vertical.pharma.msv3.server/target/docker');
                dockerImages['msv3Server'] = dockerBuildAndPush(msv3ServerDockerConf)

                final DockerConf webuiApiDockerConf = reportDockerConf
                        .withArtifactName('metasfresh-webui-api')
                        .withWorkDir('metasfresh-webui-api/target/docker');
                dockerImages['webuiApi'] = dockerBuildAndPush(webuiApiDockerConf)

                final DockerConf appDockerConf = reportDockerConf
                        .withArtifactName('metasfresh-app')
                        .withWorkDir('metasfresh-dist/dist/target/docker/app')
                dockerImages['app'] = dockerBuildAndPush(appDockerConf)

                // we now apply this build's migration scripts to the dbInit-container and push the result of that as well
                final String metasfreshDistSQLOnlyURL = "${mvnConf.deployRepoURL}/de/metas/dist/metasfresh-dist-dist/${misc.urlEncode(env.MF_VERSION)}/metasfresh-dist-dist-${misc.urlEncode(env.MF_VERSION)}-sql-only.tar.gz"
                dockerImages['db'] = applySQLMigrationScripts(
                        params.MF_SQL_SEED_DUMP_URL,
                        metasfreshDistSQLOnlyURL)

                final String dbImageDescr = dockerImages['db'] ? "<li><code>${dockerImages['db']}</code> has applied already the migration scripts from this build </li>" : "";

                currentBuild.description = """${currentBuild.description}<br/>
					This build created the following deployable docker images 
					<ul>
					<li><code>${dockerImages['app']}</code></li>
					<li><code>${dockerImages['webuiApi']}</code></li>
					${dbImageDescr}
					<li><code>${dockerImages['report']}</code> that can be used as <b>base image</b> for custom metasfresh-report docker images</li>
					<li><code>${dockerImages['msv3Server']}</code></li>
					</ul>
					"""
            } // stage build Backend

    return dockerImages
}

String applySQLMigrationScripts(
        final String sqlSeedDumpURL,
        final String metasfreshDistSQLOnlyURL) {

    final def misc = new de.metas.jenkins.Misc()
    final String specificBuildTag = misc.mkDockerTag("${env.BRANCH_NAME}-${env.MF_VERSION}")
    final String latestBuildTag = misc.mkDockerTag("${env.BRANCH_NAME}_LATEST")

    docker.withRegistry("https://${DockerConf.PUSH_REGISTRY}/v2/", DockerConf.PUSH_REGISTRY_CREDENTIALS_ID)
            {
                sh """bash -x metasfresh-dist/dist/target/docker/build_db.sh \
${DockerConf.PUSH_REGISTRY} \
${specificBuildTag} \
${latestBuildTag} \
${sqlSeedDumpURL} \
${metasfreshDistSQLOnlyURL}"""
            }
    def props = readProperties file: 'metasfresh-dist/dist/target/docker/build_sb_result_dockerimages.properties'
    return props['build_image']
}

return this;
