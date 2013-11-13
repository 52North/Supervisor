/*
 * ﻿Copyright (C) 2013 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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