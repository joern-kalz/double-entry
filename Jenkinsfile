pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile-build'
            dir 'ci-cd'
            args '-v /root/.gradle:/root/.gradle'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh "./gradlew clean build"
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
        stage('Analyse') {
            when { branch 'master' }
            steps {
                sh "./gradlew sonarqube -Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_TOKEN}"
            }
        }
        stage('Deploy') {
            when { branch 'master' }
            steps {
                sh "./gradlew ${JIB_OPTIONS} jib --image=${DOCKER_REGISTRY}/double-entry"
            }
        }
    }
}
