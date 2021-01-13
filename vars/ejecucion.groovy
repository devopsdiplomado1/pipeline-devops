def isIntegracion() {
    return ("${env.BRANCH_NAME}" =~ /(feature|develop)/)
}

def isDespliegue() {
    return ("${env.BRANCH_NAME}" =~ /(release)/)
}
def getNombreProyecto(){
    return env.GIT_URL.replaceAll('https://github.com/devopsdiplomado1/', '').replaceAll('.git', '');
} 

def isProyectoMavenOK(){
        if ((fileExists('mvnwqq')  &&  fileExists('mvnw.cmdqq'))
         return (true);
        else 
         return (false); 
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

                if (!getNombreProyecto().startsWith("ms-")) {
                    currentBuild.result = 'FAILURE'
                    error ('No se puede ejecutar este pipeline, ya que el proyecto no es de microservicios') 
                } 

                 if (isProyectoMavenOK()) {
                    currentBuild.result = 'FAILURE'
                    error ('No se puede ejecutar este pipeline, ya que el proyecto no tiene los archivos de compileación de maven') 
                }                
                

                if (isIntegracion()) {
                        echo "Entro a Integracion" 
                        integracion.call(stage);
                } else if (isDespliegue()){ 
                        echo "Entro a Despliegue"
                        despliegue.call();                 
                }  else {
                        error ('Esta rama ${env.BRANCH_NAME} no puede ejecutarse con este pipeline')
                }

                }


            }
        }
    }

    post {
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