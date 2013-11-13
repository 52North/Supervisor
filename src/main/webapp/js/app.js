var supervisorApp = angular.module('supervisorApp', [ 'ngRoute',
		'supervisorControllers' ]);

//Install database onload - config settings
supervisorApp.run(function(globals, $rootScope) {
   // Your code here....
});

//Define global variables
supervisorApp.value('globals', {
        db : null,
        databasename : "serviceapp",
        table1 : "serviceorders",
        hasdata : false,
        image_max_width: 620,
    }
);

supervisorApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/', {
		templateUrl : 'partials/result-list.html',
		controller : 'ResultListCtrl'
	// }).when('/result/:', {
	// templateUrl : 'partials/phone-detail.html',
	// controller : 'PhoneDetailCtrl'
	}).otherwise({
		redirectTo : '/'
	});
} ]);