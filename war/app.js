angular.module('project', ['server'])
	.config(function($routeProvider) {
		$routeProvider.
			when('/agenda'  , {controller: AgendaCtrl  , templaceUrl: 'html/agenda.html'  }).
			when('/calendar', {controller: CalendarCtrl, templateUrl: 'html/calendar.html'}).
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
		{name: 'Classes' , path: '/classes' },
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

function SettingsCtrl($scope, $location, server) {
}
