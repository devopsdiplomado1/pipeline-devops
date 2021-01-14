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

def call(){
pipeline {
    agent any
    parameters { 
        string(name: 'stage' , defaultValue: '', description: '')
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
                        integracion.call(stage, getNombreProyecto());
                } else if (isDespliegue()){ 
                        echo "Entro a Despliegue"
                        //Validar formato de nombre de rama release según patrón, release-v{major}-{minor}-{patch}
                        //tamara - cesar 
                        despliegue.call(stage, getNombreProyecto());                 
                }  else {
                        error ("Esta rama ${env.BRANCH_NAME} no puede ejecutarse con este pipeline")
                }

                }


            }
        }
    }

    post {
        //Tamara
        success{
            //: [Nombre Alumno][Nombre Job][buildTool] Ejecución exitosa
            slackSend color: 'good', message: "[Grupo 1][${env.JOB_NAME}]Ejecucion exitosa"           
        }

        failure{
            //[Nombre Alumno][Nombre Job][buildTool] Ejecución fallida en stage [Stage]
            slackSend color: 'danger', message: "[Grupo 1][${env.JOB_NAME}]Ejecución fallida en stage [${env.TAREA}]"                   
        }
    }

}


}

return this;