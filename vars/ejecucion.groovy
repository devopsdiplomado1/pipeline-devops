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

                                          
                if (env.BRANCH_NAME.contains('feature-') || (env.BRANCH_NAME.contains('develop') )) {
                        echo "Entro a Integracion" 
                        integracion.call(stage);
                } else if (env.BRANCH_NAME.contains('release-')){ 
                        echo "Entro a Despliegue"
                        despliegue.call();                 
                }

                }
            }
        }
    }

    post {
        success{
            //: [Nombre Alumno][Nombre Job][buildTool] Ejecución exitosa
            slackSend color: 'good', message: "[Rodrigo Zuniga][${env.JOB_NAME}][${env.HERRAMIENTA}]Ejecucion exitosa"           
        }

        failure{
            //[Nombre Alumno][Nombre Job][buildTool] Ejecución fallida en stage [Stage]
            //la variable env.TAREA esta definida en los groovy
            slackSend color: 'danger', message: "[Rodrigo Zuniga][${env.JOB_NAME}][${env.HERRAMIENTA}]Ejecución fallida en stage [${env.TAREA}]"                   
        }
    }

}


}

return this;