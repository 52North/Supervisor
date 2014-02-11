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
var supervisorControllers = angular.module('supervisorControllers', []);

// supervisorApp_test
// .controller(
// 'ResultListCtrl',
// function ResultListCtrl($scope) {
// $scope.results = [
//
// // { "results": [
// {
// "check" : "http://localhost:8080/Supervisor/api/v1/checks/0f7zaxpuoz",
// "result" : " ... Response was not a Capabilities document!",
// "type" : "NEGATIVE",
// "checkTime" : "Wed Nov 13 17:01:04 CET 2013"
// },
// {
// "check" : "http://localhost:8080/Supervisor/api/v1/checks/ftwwpi0myj",
// "result" : "Self check ran succesfully, service is most probably up and
// running. Go to <a href='http://localhost:8080/Supervisor'
// title='OwsSupervisor HTML Interface'>http://localhost:8080/Supervisor</a> for
// the current check status. *** Heap Info: Size (Mb) is 208 of 1776 leaving
// 162.",
// "type" : "POSITIVE",
// "checkTime" : "Wed Nov 13 17:01:04 CET 2013"
// },
// {
// "check" : "http://myhost:8080/Supervisor/api/v1/checks/ftwwpi0myj",
// "result" : "some random test *** Info: lala",
// "type" : "POSITIVE",
// "checkTime" : "Wed Nov 13 17:01:04 CET 2013"
// }
// // ] }
//
// ];
//
// $scope.orderProp = 'checkTime';
// });

supervisorControllers.controller('ResultListCtrl', [ '$scope', '$http',
		function ResultListCtrl($scope, $http) {
			$http.get('api/v1/results?expanded=true').success(function(data) {
				$scope.results = data['results'];
			});
			
			$scope.orderProp = 'checkTime';

			$scope.getClass = function(type) {
				if (type === "NEGATIVE") {
					return "alert alert-danger";
				}
				if (type === "POSITIVE") {
					return "alert alert-success";
				}
				if (type === "NEUTRAL") {
					return "alert alert-info";
				} else {
					return "alert";
				}
			};
		} ]);

supervisorControllers.controller('ResultDetailCtrl', [ '$scope',
		'$routeParams', function($scope, $routeParams) {
			$scope.checkId = "test";// $routeParams.check;
		} ]);