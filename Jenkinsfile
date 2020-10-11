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
                sh './gradlew build'
            }
        }
    }
}