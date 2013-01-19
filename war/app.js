angular.module('project', ['server'])
	.config(function($routeProvider) {
		$routeProvider.
			when('/agenda'  , {controller: AgendaCtrl  , templaceUrl: 'html/agenda.html'  }).
			when('/calendar', {controller: CalendarCtrl, templateUrl: 'html/calendar.html'}).
			when('/settings', {controller: SettingsCtrl, templateUrl: 'html/settings.html'}).
			otherwise({redirectTo: '/agenda'});
	})
	.run(function() {
		
		/* google analytics snippet */
		var _gaq = [['_setAccount', 'UA-19517403-4'], ['_trackPageview']];
		(function(d, t) {
			var g = d.createElement(t),
			    s = d.getElementsByTagName(t)[0];
			g.src = '//www.google-analytics.com/ga.js';
			s.parentNode.insertBefore(g, s);
		}(document, 'script'));
	});
	

function AppCtrl($scope, $location, server) {
	$scope.$watch(function() { return server.logged_in; }, function(logged_in) {
		$scope.logged_in = server.user.logged_in;
	});
	$scope.login = function() {
		server.user.login();
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
