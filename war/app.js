"use strict";

var app = angular.module('eDAB-app', ['eDAB-utils']);

app.config(function($routeProvider) {
		$routeProvider.
			when('/agenda'  , {controller: AgendaCtrl  , templateUrl: 'partials/agenda.html'  }).
			when('/calendar', {controller: CalendarCtrl, templateUrl: 'partials/calendar.html'}).
			when('/courses' , {controller: CoursesCtrl , templateUrl: 'partials/courses.html' }).
			when('/settings', {controller: SettingsCtrl, templateUrl: 'partials/settings.html'}).
			otherwise({redirectTo: '/agenda'});
	});

app.service('appService', function($http, $window, $q) {
	var self = this;
	
	// Default handling for authorized requests.
	self._reqHandler = {
		success: function(response) {
			self.auth.logged_in = true;
			return response;
		},
		error: function(response) {
			if (response.data.error) {
				console.info("Request error", response.data.error);
				
				// If the session needs to be initialized, we can retry it. But only retry once.
				if (response.data.error.name == "InvalidSession" && !response.config.retry) {
					console.log("Invalid session. Will retry request");
					response.config.retry = true;
					return $http(response.config)
						.then(self._reqHandler.success, self._reqHandler.error);
				}
				
				// If we're not logged in
				else if (response.data.error.name == "NotLoggedIn") {
					self.auth.logged_in = false;
				}
				
				else if (response.data.error.name == "NeedsApproval") {
					alert("Lalala you need to be manually approved first");
				}
			}
			
			// If we don't otherwise handle it...
			return $q.reject(response);
		}
	};
	
	self.auth = {
		
		logged_in: null, // null=loading, true, false
		
		user: null,
		
		login: function() {
			// $window.location.replace("/login");
			$window.open("/login?close", "login");
			
			$window.loginCallback = function() {
				self.auth.check();
			};
		},
			
		logout: function() {
			$http({
				method: "POST",
				url: "/logout"
			})
			.then(function(resp) {
				self.auth.check();
			});
		},
		
		check: function() {
			$http({
				method: "GET",
				url: "/rest/account/currentUser"
			})
			.then(self._reqHandler.success, self._reqHandler.error)
			.then(function(response) {
				console.log("Loaded user - response:", response);
				self.auth.user = response.data;
			});
		}
	};	
})
app.run(function(appService) {
	appService.auth.check();
});

		/*
		check: function () {
			//_this.auth.logged_in = false;
			_this.rpc._request("getCurrentUser", null)
				.then(function(resp) {
					if (resp.data && resp.data.result && !resp.data.error) {
						console.log(resp);
						_this.auth.logged_in = true;
						_this.user.name = resp.data.result.name;
						_this.user.fcps_id = resp.data.result.fcps_id;
						
						$location.path("/settings");
					} else {
						console.error(resp);
						_this.auth.logged_in = false;
						try {
							$rootScope.showError(JSON.stringify(resp.data.error));
						} catch(e) {}
					}
				});
		}
	};
	_this.user = {
		name: null,
		fcps_id: null
	};
	_this.rpc = {
		_counter: 0,
		_request: function(method, params) {
			return $http({
				method: "POST",
				url: RPC_URL,
				data: {
					"jsonrpc": "2.0",
					"method": method,
					"params": params,
					"id": _this.rpc._counter++
				}
			});
		}
	};
	$window._rpcRequest = function(method, params) {
		return _this.rpc._request(method, params);
	};
})
.run(function(server) {
	server.auth.check();
});
*/


function AppCtrl($scope, appService, $exceptionHandler) {
	$scope.service = appService;
	
	$scope.infoboxState = function() {
		if (appService.auth.logged_in === false) {
			return "login";
		} else if (appService.auth.logged_in === null) {
			return "loading";
		}
		return false;
	};
	
	$scope.getErrorMsg = function() {
		return $exceptionHandler.errorMsg;
	};
}

function NavCtrl($scope, $location) {
	$scope.onPage = function(path) {
		return $location.path == path;
	};
	
	$scope.pages = [
		{name: 'Agenda'  , path: '/agenda'  },
		{name: 'Calendar', path: '/calendar'},
		{name: 'Courses' , path: '/courses' },
		{name: 'Settings', path: '/settings'}
	];
}

function AgendaCtrl($scope, $location, appService) {
}

function CalendarCtrl($scope, $location, appService) {
}

function CoursesCtrl($scope, $location, appService) {
}

function SettingsCtrl($scope, $location, appService) {	
	$scope.service = appService;
	$scope.user = angular.copy(appService.auth.user);
	
	$scope.schools = [
		{"name": "Rachel Carson MS"}
	];
	$scope.teams = [
		{"grade": 7, "letter": "A", "name": "Star Jumps"},
		{"grade": 7, "letter": "B", "name": "Trail Mix"},
		{"grade": 7, "letter": "C", "name": "Dictionaries"},
		{"grade": 7, "letter": "D", "name": "Dream On"},
		{"grade": 7, "letter": "E", "name": "Eggplants"},
		{"grade": 8, "letter": "V", "name": "Valedictors"},
		{"grade": 8, "letter": "W", "name": "Narwhals"},
		{"grade": 8, "letter": "X", "name": "Radiation"},
		{"grade": 8, "letter": "Y", "name": "Bumblebees"},
		{"grade": 8, "letter": "Z", "name": "Zebras"}
	];
	
	$scope.isUnchanged = function() {
		return angular.equals(appService.auth.user, $scope.user);
	};
	$scope.save = function() {
		appService.auth.user = angular.copy($scope.user);
		
		// and other stuff
	};
	$scope.reset = function() {
		$scope.user = angular.copy(appService.auth.user);
	};
	
	$scope.$watch("appService.auth.user", function() {
		$scope.reset();
	});
}
