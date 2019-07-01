pipeline {
    agent any
    stages {
        stage('Checkout sources from Subversion') {
            steps {
                checkout([$class: 'SubversionSCM',
                    additionalCredentials: [],
                    excludedCommitMessages: '',
                    excludedRegions: '',
                    excludedRevprop: '',
                    excludedUsers: '',
                    filterChangelog: false,
                    ignoreDirPropChanges: false,
                    includedRegions: '',
                    locations: [[credentialsId: '5e04d533-8a09-43b9-b247-9e25294a4327',
                        depthOption: 'infinity',
                        ignoreExternalsOption: true,
                        local: '.',
                        remote: "svn://120.26.95.228/sources/service/zcareu-service/center-report"]],
                    workspaceUpdater: [$class: 'UpdateUpdater']])
            }
        }

        stage('Testing') {
            steps {
                script {
                    if (isUnix()) {
                        sh './grailsw clean'
                        sh './grailsw test-app'
                    } else {
                        bat 'grailsw.bat clean'
                        bat 'grailsw.bat test-app'
                    }
                }
                publishHTML (target: [
                  allowMissing: false,
                  alwaysLinkToLastBuild: false,
                  keepAll: true,
                  reportDir: 'build/reports/tests',
                  reportFiles: 'index.html',
                  reportName: "Test Report"
                ])
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'
                    tapdTestReport frameType: 'JUnit', onlyNewModified: true, reportPath: 'build/test-results/**/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean build -x test -x integrationTest -x codenarcIntegrationTest -x codenarcMain -x codenarcTest -x check'
                    } else {
                        bat 'gradlew.bat clean build -x test -x integrationTest -x codenarcIntegrationTest -x codenarcMain -x codenarcTest -x check'
                    }
                }
                archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/libs/*.*ar'
            }
        }

//        stage('Deploy war file to remote tomcat') {
//            steps {
//                ansiblePlaybook disableHostKeyChecking: true, credentialsId: '5c010bf0-4a85-4ab0-8edc-4aa636865c4a', installation: 'Ansible', inventory: 'ansible-hosts', playbook: 'ansible-tomcat-deploy.yml'
//            }
//        }

        stage('Code Coverage') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean'
                        sh './gradlew check'
                        sh './gradlew cloverGenerateReport'
                    } else {
                        bat 'gradlew.bat clean'
                        bat 'gradlew.bat check'
                        bat 'gradlew.bat cloverGenerateReport'
                    }
                }
                step([
                    $class: 'CloverPublisher',
                    cloverReportDir: 'build/reports/clover',
                    cloverReportFileName: 'clover.xml',
                    healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80],   // optional, default is: method=70, conditional=80, statement=80
                    unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                    failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]       // optional, default is none
                  ])
              }
        }
    }
}