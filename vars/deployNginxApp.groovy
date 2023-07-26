def call(String DOCKER_REGISTRY, String DOCKER_IMAGE_NAME, String DOCKER_IMAGE_TAG, String K8S_NAMESPACE, String K8S_DEPLOYMENT_NAME) {
    pipeline {
        agent any

        stages {
            stage('Build Docker Image') {
                steps {
                    script {
                        docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}", './nginx')
                    }
                }
            }

            stage('Push Docker Image to Nexus') {
                steps {
                    script {
                        docker.withRegistry("${DOCKER_REGISTRY}", 'nexus-credentials') {
                            dockerImagePush("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                        }
                    }
                }
            }

            stage('Deploy to Kubernetes') {
                steps {
                    script {
                        sh "minikube start"
                        sh "kubectl config use-context minikube"
                        sh "kubectl create namespace ${K8S_NAMESPACE} || true"
                        sh "kubectl apply -f kubernetes/deployment.yaml -n ${K8S_NAMESPACE}"
                    }
                }
            }
        }
    }
}

