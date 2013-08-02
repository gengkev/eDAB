"use strict";

var app = angular.module('eDAB-app', ['eDAB-utils']);

app.config(function($routeProvider, $locationProvider) {
		$routeProvider
			.when('/agenda', {controller: AgendaCtrl, templateUrl: '/partials/agenda.html'})
			.when('/calendar', {controller: CalendarCtrl, templateUrl: '/partials/calendar.html'})
			.when('/courses', {controller: CoursesCtrl, templateUrl: '/partials/courses.html'})
			.when('/settings', {controller: SettingsCtrl, templateUrl: '/partials/settings.html'})
			.when('/u/:username', {controller: UserCtrl, templateUrl: '/partials/user.html'})
			.otherwise({redirectTo: '/agenda'});
		
		$locationProvider.html5Mode(true);
	});

// This needs to be cleaned up. Big time.
app.service('appService', function($http, $window, $q, $rootScope) {
	var self = this;
	
	// Default handling for authorized requests.
	self._reqHandler = {
		success: function(response) {
			self.auth.logged_in = true;
			return response;
		},
		error: function(response) {
			console.info("Request error", response.data.error);
			
			if (response.data.error) {
				
				// If the session needs to be initialized, we can retry it. But only retry once.
				if (response.data.error.name == "InvalidSession" && !response.config.retry) {
					console.log("Invalid session. Will retry request");
					response.config.retry = true;
					return $http(response.config)
						.then(self._reqHandler.success, self._reqHandler.error);
				}
				
				// If we're not logged in
				else if (response.data.error.name == "NotLoggedIn") {
					self.auth.user = null;
					self.auth.logged_in = false;
				}
				
				else if (response.data.error.name == "NeedsApproval") {
					alert("Lalala you need to be manually approved first");
				}
				
				else {
					throw response.data.error;
				}
				
			} else {
				throw {
					name: response.status,
					message: "Unexpected HTTP error"
				};
			}
			
			return $q.reject(response);
		}
	};
	
	self.auth = {
		
		logged_in: null, // null=loading, true, false
		
		user: null,
		
		updateUser: function(newUser) {
			newUser = angular.copy(newUser);
			
			return $http({
				method: "PUT",
				url: "/rest/account/currentUser",
				data: newUser
			})
			.then(self._reqHandler.success, self._reqHandler.error)
			.then(function(response) {
				console.log("Saved user. Response: ", response);
				self.auth.user = newUser;
			});
		},
		
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
				console.log("Loaded user. Response:", response);
				self.auth.user = response.data;
			});
		}
	};
	
});

app.run(function(appService, $rootScope) {
	appService.auth.check();
	
	$rootScope.$watch(function() { return appService.auth.logged_in; }, function(newVal) {
		$rootScope.$broadcast("loginStateChange", newVal);
	});
});


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
	$scope.onPage = function(page) {
		return $location.path() == page.path;
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

function SettingsCtrl($scope, $location, appService, $window, Utils) {	
	$scope.service = appService;
	$scope.user = angular.copy(appService.auth.user);
	
	$scope.$on("loginStateChange", function(newState) {
		$scope.user = angular.copy(appService.auth.user);
	});
	
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
		// so people don't accidentally submit twice
		$scope.disableButtons = true;
		
		appService.auth.updateUser($scope.user)
		.then(function() {
			$scope.disableButtons = false;
		}, function() {
			$scope.disableButtons = false;
		});
	};
	$scope.reset = function() {
		$scope.user = angular.copy(appService.auth.user);
	};
	
	$scope.$watch("appService.auth.user", function() {
		$scope.reset();
	});
	
	// Initialize Google+ badge
	Utils.loadGPlusBadge();
}

function UserCtrl($scope, $routeParams, $http, $location, appService) {
	var username = $routeParams.username;
	$http({
		method: "GET",
		url: "/rest/users/" + username
	})
	.then(function(response) {
		console.log("Loaded user: ", response);
		$scope.user = response.data;
	}, function(response) {
		if (response.status == 404) {
			alert("User not found!");
			$location.path("/");
		} else {
			// uh idk
			appService._reqHandler.error(response);
		}
	});
}