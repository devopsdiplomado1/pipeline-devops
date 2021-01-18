def crearRamaGit(String origin, String newRranch){
   try {
        sh '''
            git fetch -p
            git checkout '''+origin+'''; git pull
        '''
    } catch (Exception a){ } 



    try {
        sh '''
            git checkout -b '''+newRranch+'''
            git push -u origin (HEAD detached at origin/'''+newRranch+''')
        '''
    } catch (Exception a){ } 

}




def diffRama(String rama){
    try {        
        sh "git diff remotes/origin/${rama} origin/main"
    } catch (Exception a){ }   
    
}

def borrarRama(String rama){
    
    try {
        sh "git fetch -p"
        echo "borrando rama con -d"
        sh "git branch -d ${rama}"
        sh "git tag -d ${rama}"
    } catch (Exception a){ }   

    try {
        echo "borrando rama con -delete"
        sh "git push origin --delete ${rama}"
    } catch (Exception a){ }      

     try {
         echo "borrando rama con -delete y remotes"
         sh "git push origin --delete refs/remotes/origin/${rama}"
    } catch (Exception a){ } 

         try {
         echo "borrando rama con -delete y remotes2"
         sh "git push origin : refs/heads/${rama}"
    } catch (Exception a){ }  

     try {
        sh "git fetch -p"
    } catch (Exception a){ }  
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
    if ("${listarRamas()}".contains("${rama}"))
        existe = true
    /* este comando nos daba error de permisos
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
def referenciaMain(){
    try {
        sh "git fetch origin main:refs/remotes/origin/main"
    } catch (Exception a){ } 
}

def referenciaDevelop(){
    try {
        sh "git fetch origin develop:refs/remotes/origin/develop"
    } catch (Exception a){ } 
}

def referenciaRelease(String targetBranch){
    try {
        sh "git fetch origin ${targetBranch}:refs/remotes/origin/${targetBranch}"
    } catch (Exception a){ } 
}


def crearMerge(String originBranch, String targetBranch){
    try {
        sh "git fetch origin main:refs/remotes/origin/main"

        sh '''
            git fetch -p
            git checkout '''+targetBranch+'''
            git pull
            git merge '''+originBranch+'''
            git push origin '''+targetBranch+'''
        '''
    } catch (Exception a){ } 
}


def tagGit(String origin, String newRranch){
    try {
        sh '''
            git checkout '''+origin+'''
            git tag release-v1-0-0            
            git push '''+origin+''' --tags
            git tag

        '''
       
    } catch (Exception a){ } 


}