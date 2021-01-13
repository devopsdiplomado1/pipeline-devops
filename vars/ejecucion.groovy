def isIntegracion() {
    return ("${env.BRANCH_NAME}" =~ /(feature|develop)/)
}

def isDespliegue() {
    return ("${env.BRANCH_NAME}" =~ /(release)/)
}
def getNombreProyecto(){
    return env.GIT_URL.replaceAll('https://github.com/devopsdiplomado1', '').replaceAll('.git', '');
}    


def call(){
pipeline {
    agent any
    parameters { 
        string(name: 'stage' , defaultValue: '', description: '')
    }


    stages {
        /*
        stage("Env Variables") {
            steps {
                sh "printenv"
            }
        }
        */

        stage('Pipeline') {
            steps {
                script {

                env.TAREA = ''
                echo "A.-Parametros seleccionados: ${stage}"   
                echo "B.-Running ${env.BUILD_ID} on ${env.JENKINS_URL}"   
                echo "C.-Rama ${env.BRANCH_NAME}" 
                echo "D.-Nombre del projecto ${getNombreProyecto()}" 

                /*if () {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar este pipeline, ya que no ingreso parametros conocidos"
                } 
                */  
/*
                switch(env.BRANCH_NAME){
                    case 'feature*':
                        echo "Entro a Integracion" 
                        integracion.call(stage)
                        break                    
                    case 'develop':    
                        echo "Entro a Integracion" 
                        integracion.call(stage)
                        break
                    case 'release-*':
                        echo "Entro a Despliegue"
                        despliegue.call()
                        break
                    case 'main':
                    case 'master':
                        error ('Esta rama ${env.BRANCH_NAME} no puede ejecutarse con este pipeline')    

                }   


*/

                if (isIntegracion()) {
                        echo "Entro a Integracion" 
                        integracion.call(stage);
                } else if (isDespliegue()){ 
                        echo "Entro a Despliegue"
                        despliegue.call();                 
                }  else {
                    echo " La rama <${env.GIT_BRANCH}> no puede ejecutarse con este pipeline" 
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