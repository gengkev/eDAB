angular.module('project', [])
	.config(function($routeProvider) {
		$routeProvider.
			when('/agenda'  , {controller: AgendaCtrl  , templaceUrl: 'html/agenda.html'  }).
			when('/calendar', {controller: CalendarCtrl, templateUrl: 'html/calendar.html'}).
			when('/settings', {controller: SettingsCtrl, templateUrl: 'html/settings.html'}).
			otherwise({redirectTo: '/agenda'});
	})
	.factory('client', function($window) {
		return {
			login: function() {
				$window.location.replace("login.jsp");
			}
		};
	})
	.run(function(client) {
		client.logged_in = false;
	});

function AppCtrl($scope, $location, client) {
	$scope.$watch(function() { return client.logged_in; }, function(logged_in) {
		$scope.logged_in = client.logged_in;
	});
	$scope.login = function() {
		client.login();
	};
}

function NavCtrl($scope, $location, client) {
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

function LandingCtrl($scope, $location, client) {
}

function AgendaCtrl($scope, $location, client) {
}

function CalendarCtrl($scope, $location, client) {
}

function SettingsCtrl($scope, $location, client) {
}