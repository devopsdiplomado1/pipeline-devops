def isIntegracion() {
    return ("${env.BRANCH_NAME}" =~ /(feature|develop)/)
}

def isDespliegue() {
    return ("${env.BRANCH_NAME}" =~ /(release*)/)
}
def getNombreProyecto(){
    return env.GIT_URL.replaceAll('https://github.com/devopsdiplomado1/', '').replaceAll('.git', '');
} 

def isProyectoMavenOK(){
         return ( (fileExists('mvnw')  &&  fileExists('mvnw.cmd')) );
}

def cumplePatron(){
	return ("${env.BRANCH_NAME}" =~ /release-v\d{1,2}\-\d{1,2}\-\d{1,3}/)
}

def call(){
pipeline {
    agent any
    parameters { 
        string{name: 'stage', defaultValue: '', description: ''}
		string{name: 'pipeline', defaultValue: '', description: ''}
    }


    stages {

        stage('Pipeline') {
            steps {
                script {

                env.TAREA = ''
                echo "A.-Stages seleccionados: ${stage}"   
                echo "B.-Running ${env.BUILD_ID} on ${env.JENKINS_URL}"   
                echo "C.-Rama ${env.BRANCH_NAME}" 
                echo "D.-Nombre del projecto ${getNombreProyecto()}" 
                echo "E.-Estan los archivos maven? ${isProyectoMavenOK()}" 

                if (!getNombreProyecto().startsWith("ms-")) {
                    currentBuild.result = 'FAILURE'
                    error ('No se puede ejecutar este pipeline, ya que el proyecto no es de microservicios') 
                } 

                 if (!isProyectoMavenOK()) {
                    currentBuild.result = 'FAILURE'
                    error ('No se puede ejecutar este pipeline, ya que el proyecto no tiene los archivos de compilación de maven') 
                }                
                

                if (isIntegracion()) {
                        echo "Entro a Integracion" 
						pipeline = 'CI'
                        integracion.call(stage, getNombreProyecto());
                } else if (isDespliegue()){ 
                        echo "Entro a Despliegue"
                        pipeline = 'CD'
						if (cumplePatron()){
							despliegue.call(stage, getNombreProyecto());
						} else {
							error ("La rama release no cumple con el patrón release-v{major}-{minor}-{patch}")
						}
                }  else {
                        error ("Esta rama ${env.BRANCH_NAME} no puede ejecutarse con este pipeline")
                }

                }


            }
        }
    }

    post {
        success{
            // [Grupo 1][Pipeline CI/Release][Rama: nombreRama][Stage: nombreStage][Resultado: OK]
            slackSend channel: "#lab-pipeline-status-grupo1", color: 'good', message: "[Grupo 1][Pipeline ${params.pipeline}][Rama: ${env.BRANCH_NAME}][Stage: ${env.TAREA}][Resultado: OK]"           
        }

        failure{
            // [Grupo 1][Pipeline CI/Release][Rama: nombreRama][Stage: nombreStage][Resultado: No OK]
            slackSend channel: "#lab-pipeline-status-grupo1", color: 'danger', message: "[Grupo 1][Pipeline ${params.pipeline}][Rama: ${env.BRANCH_NAME}][Stage: ${env.TAREA}][Resultado: No OK]"                   
        }
    }

}


}

return this;