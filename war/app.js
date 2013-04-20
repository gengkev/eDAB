angular.module('project', ['server'])
	.config(function($routeProvider) {
		$routeProvider.
			when('/agenda'  , {controller: AgendaCtrl  , templateUrl: 'html/agenda.html'  }).
			when('/calendar', {controller: CalendarCtrl, templateUrl: 'html/calendar.html'}).
			when('/courses' , {controller: CoursesCtrl , templateUrl: 'html/courses.html' }).
			when('/settings', {controller: SettingsCtrl, templateUrl: 'html/settings.html'}).
			otherwise({redirectTo: '/agenda'});
	})
	.run(function($window) {
		
		/* google analytics snippet */
		$window._gaq = [['_setAccount', 'UA-19517403-4'], ['_trackPageview']];
		(function(d, t) {
			var g = d.createElement(t),
			    s = d.getElementsByTagName(t)[0];
			g.src = '//www.google-analytics.com/ga.js';
			s.parentNode.insertBefore(g, s);
		}($window.document, 'script'));
		
	}).directive("fadeInOut", function($timeout) {
		return function(scope, element, attrs) {
			var doShow = true, isShowing = true, 
			    timeout = parseInt(attrs.timeout) || 2000,
			    timeoutId;
			
			function show() {
				if (timeoutId) $timeout.cancel(timeoutId);
				
				element[0].style.transition = (timeout / 1000) + "s ease";
				element[0].style.display = "block";
				
				$timeout(function() {
					element[0].style.opacity = "1";
				}, 0);
			}
			function hide() {
				if (timeoutId) $timeout.cancel(timeoutId);
				
				element[0].style.transition = (timeout / 1000) + "s ease";
				element[0].style.opacity = "0";
				
				timeoutId = $timeout(function() {
					timeoutId = null;
					element[0].style.display = "none";
				}, timeout);
			}
			
			scope.$watch(attrs.fadeInOut, function(value) {
				var newDoShow = !!value;
				console.log("newDoShow", newDoShow);
				if (newDoShow != doShow) {					
					if (newDoShow) show();
					else hide();
					
					doShow = newDoShow;
				}
			});
		};
	});
	

function AppCtrl($scope, $rootScope, $location, $window, server) {
	$scope.$watch(function() { return server.auth.logged_in; }, function(logged_in) {
		$scope.logged_in = logged_in;
	});
	$scope.$watch(function() { return server.user; }, function(user) {
		$scope.user = user;
	});
	$scope.login = function() {
		server.auth.login();
	};
	$scope.logout = function() {
		server.auth.logout();
	};
	
	$window.bla = function() {
		return server.rpc._request("idk", []);
	};
	
	$scope.errorMsg = null;
	(function() {
		var timeout = null;
		$rootScope.showError = function(str, time) {
			if (timeout) {
				$window.clearTimeout(timeout);
			}
			$scope.errorMsg = str;
			timeout = $window.setTimeout(function() {
				$scope.errorMsg = null;
			}, time || 10000);
		};
	}());
	
	$scope.infoboxState = function() {
		if ($scope.logged_in === false) {
			return "login";
		} else if ($scope.logged_in === null) {
			return "loading";
		}
		return false;
	};
}

function NavCtrl($scope, $location, server) {
	$scope.$watch(function() { return $location.path(); }, function(path) {
		$scope.currentPage = path;
	});
	$scope.pages = [
		{name: 'Agenda'  , path: '/agenda'  },
		{name: 'Calendar', path: '/calendar'},
		{name: 'Courses' , path: '/courses' },
		{name: 'Settings', path: '/settings'}
	];
	$scope.onPage = function(path) {
		return $scope.currentPage == path;
	};
}

function LandingCtrl($scope, $location, server) {
}

function AgendaCtrl($scope, $location, server) {
}

function CalendarCtrl($scope, $location, server) {
}

function CoursesCtrl($scope, $location, server) {
}

function SettingsCtrl($scope, $location, server) {
	$scope.nameChangeRequest = function() {
		alert("Sorry, Jenny.");
	};
	$scope.schools = [
		{"name": "Rachel Carson MS"}
	];
	$scope.user.school = $scope.schools[0];
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
}