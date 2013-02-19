modules = {
	base {
		dependsOn 'jquery'
		resource url: '/css/cluetip.css'
		resource url: '/js/jquery.cluetip.min.js', disposition: 'head'
		resource url: '/js/application.js', disposition: 'head'

	}
	
	core {
		dependsOn 'base'
		
		resource url: '/less/main.less', attrs: [rel: 'stylesheet/less', type: 'css']
	}
	
	intro {
		dependsOn 'base'
		
		resource url: '/less/intro.less', attrs: [rel: 'stylesheet/less', type: 'css']
	}
	
	errors {
		resource url: '/less/errors.less', attrs: [rel: 'stylesheet/less', type: 'css']
	}
	
	dynatree {
		dependsOn 'core, jquery-ui'
		
		resource url: '/js/skin-vista/ui.dynatree.css'
        resource url: '/js/jquery.dynatree.min.js', disposition: 'head'
	}
}
