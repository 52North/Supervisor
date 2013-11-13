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
			$http.get('api/v1/results').success(function(data) {
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