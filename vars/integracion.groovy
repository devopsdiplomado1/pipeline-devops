def crearRamaGit(String origin, String newRranch){
    sh '''
        git fetch -p
        git checkout '''+origin+'''; git pull
        git checkout -b '''+newRranch+'''
        git push origin '''+newRranch+'''
    '''
}

def borrarRama(String rama){
  sh "git push origin --delete ${rama}"
}

def listarRamas(){

    def getBranches = ("git ls-remote https://github.com/devopsdiplomado1/ms-iclab.git").execute()

    def branchNameList = getBranches.text.readLines().collect {
     it.split()[1]
    }
    def releaseBranchList = branchNameList.findAll { it =~ /refs\/heads\/release*/ }

    def humanReadableReleaseBranchList = releaseBranchList.collect {
    it.replaceAll('refs/heads/', '')
    }
    echo "${humanReadableReleaseBranchList}"
    return humanReadableReleaseBranchList
}

def chequearSiExisteRama(String rama){
    def existe = false
    if ("${listarRamas()}  =~ /(${rama})/)"
        existe = true
    /*
    try {
        def output = sh (script: "git ls-remote --heads ${rama}", returnStdout: true)
        if (output?.trim()) {
		    existe = true;
	    } 
    } catch (Exception a){
        existe = false;

    }*/

	return existe

}







def call(stageOptions, nameProject){
   def buildEjecutado = false;
   def projectKey = "${nameProject}-${env.BRANCH_NAME}-${env.BUILD_ID}"
   //release-v{major}-{minor}-{patch}
   def nameRelease = "release-v1-0-0"
  
         stage("Validar"){

           if (
                stageOptions.contains('Compile')           ||
                stageOptions.contains('unitTest')          ||
                stageOptions.contains('jar')               ||
                stageOptions.contains('sonar')             ||
                stageOptions.contains('nexusUpload')       || 
                (stageOptions =='')
               ) {
               echo "Ok, se continua con los stage, ya que ingreso parametros conocidos"
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se puede ejecutar este pipeline, ya que no ingreso parametros conocidos')
            }   

       }

        stage('Compile') {
                env.TAREA =  env.STAGE_NAME 
                buildEjecutado =false;
                echo "STAGE ${env.STAGE_NAME}"
                if (stageOptions.contains('Compile Code') || (stageOptions ==''))  { 
                    sh 'mvn clean compile -e'                    
                }
        }

        stage('unitTest') {     
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}" 
                if ((stageOptions.contains('Test') || (stageOptions =='')) ) {      
                    sh 'mvn clean test -e'
                } 
        }
        stage('jar') {        
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}"
                if ((stageOptions.contains('Test') || (stageOptions ==''))) {      
                    sh 'mvn clean package -e' 
                    buildEjecutado =true;
                }
        }
        stage('sonar') {
            env.TAREA =  env.STAGE_NAME 
            echo "STAGE ${env.STAGE_NAME}"
            if (!buildEjecutado) {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar Sonar sin haber ejecutado un Build"
                buildEjecutado = false;
            }    

            def scannerHome = tool 'sonar-scanner';    
            withSonarQubeEnv('sonar-server') { 
                if ((stageOptions.contains('Sonar') || (stageOptions =='')) && (buildEjecutado) ) {                 
                    echo "Aplicando Sonar al proyecto:${projectKey}"
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectKey} -Dsonar.java.binaries=build"   
                   }    
            }  
        }         

        stage('nexusUpload') {  
            env.TAREA =  env.STAGE_NAME 
            echo "STAGE ${env.STAGE_NAME}"  
            if ((stageOptions.contains('nexusUpload') || (stageOptions =='')) && (buildEjecutado) )          
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]                     
        }  

        stage('gitCreateRelease') {  
            //rodrigo
            if ( ("${env.BRANCH_NAME}" =~ /(develop)/) && (stageOptions.contains('gitCreateRelease') || (stageOptions =='')) && (buildEjecutado) ) {
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}"
                echo "entro a gitCreateRelease" 

                if (chequearSiExisteRama("${nameRelease}")) {
                    borrarRama("${nameRelease}")
                    crearRamaGit("${env.GIT_BRANCH}", "${projectRelease}");
                } else {
                    crearRamaGit("${env.GIT_BRANCH}", "${projectRelease}");
                }
                
                //Este stage sólo debe estar disponible para la rama develop.  
                //Si IC de develop OK = ejecución de pipeline CD para rama Release creada  
            }         
        }                    

}

return this;