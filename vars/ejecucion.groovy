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
                //segun el valor del parametro se debe llamar a gradle o maven
                env.TAREA = ''
                echo "1.-PARAMETROS SELECCIONADOS: ${stage}"   
                echo "2.-Running ${env.BUILD_ID} on ${env.JENKINS_URL}"   
                echo "3.-Rama ${env.BRANCH_NAME}" 

                switch(env.BRANCH_NAME){
                    case 'feature-*':
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

/*
                if (env.BRANCH_NAME.contains('feature-') || (env.BRANCH_NAME.contains('develop') )) {
                        echo "Entro a Integracion" 
                        integracion.call(stage);
                } else if (env.BRANCH_NAME.contains('release-')){ 
                        echo "Entro a Despliegue"
                        despliegue.call();                 
                }  else {
                    echo " La rama <${env.GIT_BRANCH}> no se proceso" 
                }
*/
                }
            }
        }
    }

    post {
        success{
            //: [Nombre Alumno][Nombre Job][buildTool] Ejecución exitosa
            slackSend color: 'good', message: "[Grupo 1][${env.JOB_NAME}][${env.HERRAMIENTA}]Ejecucion exitosa"           
        }

        failure{
            //[Nombre Alumno][Nombre Job][buildTool] Ejecución fallida en stage [Stage]
            slackSend color: 'danger', message: "[Grupo 1][${env.JOB_NAME}][${env.HERRAMIENTA}]Ejecución fallida en stage [${env.TAREA}]"                   
        }
    }

}


}

return this;