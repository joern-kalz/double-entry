pipeline {
    agent {
        docker {
            image 'ubuntu:20.04'
            args '-v /root/.gradle:/root/.gradle'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
    }
}