pipeline {
    agent {
        docker {
            image 'openjdk:11'
            args '-v /root/.gradle:/root/.gradle'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh "./gradlew ${JIB_OPTIONS} jib --image=${DOCKER_REGISTRY}/double-entry"
            }
        }
    }
}