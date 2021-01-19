def isIntegracion() {
    return ("${env.GIT_BRANCH}" =~ /(feature|develop)/)
}

def isDespliegue() {
    return ("${env.GIT_BRANCH}" =~ /(release*)/)
}
def getNombreProyecto(){
    return env.GIT_URL.replaceAll('https://github.com/devopsdiplomado1/', '').replaceAll('.git', '');
} 

def isProyectoMavenOK(){
    return ( (fileExists('mvnw')  &&  fileExists('mvnw.cmd')) );
}

def cumplePatron(){
    return ("${env.GIT_BRANCH}" =~ /release-v\d{1,2}\-\d{1,2}\-\d{1,3}/)
}

def call(){
pipeline {
    agent any
    parameters { 
        string(name: 'stage', defaultValue: '', description: '')
    }


    stages {
	
        stage('Pipeline') {
            steps {
                script {
				env.NOM_PIPELINE = ''
                env.TAREA = ''
                echo "A.-Stages seleccionados: ${stage}"   
                echo "B.-Running ${env.BUILD_ID} on ${env.JENKINS_URL}"   
                echo "C.-Rama ${env.GIT_BRANCH}" 
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
						env.NOM_PIPELINE = 'IC'
                        integracion.call(stage, getNombreProyecto());
                } else if (isDespliegue()){ 
			            echo "Entro a Despliegue"
			            env.NOM_PIPELINE = 'Release'
			            if (cumplePatron()){
				            despliegue.call(stage, getNombreProyecto());
			            } else {
				            error ("La rama release no cumple con el patrón release-v{major}-{minor}-{patch}")
			            }
                }  else {
                        error ("Esta rama ${env.GIT_BRANCH} no puede ejecutarse con este pipeline")
                }

                }


            }
        }
    }

    post {
        success{
            slackSend color: 'good', message: "[Grupo 1][Pipeline ${env.NOM_PIPELINE}][Rama: ${env.GIT_BRANCH}][Stage: ${env.TAREA}][Resultado: OK]"           
        }

        failure{
            slackSend color: 'danger', message: "[Grupo 1][Pipeline ${env.NOM_PIPELINE}][Rama: ${env.GIT_BRANCH}][Stage: ${env.TAREA}][Resultado: No OK]"                   
        }
    }

}


}

return this;
