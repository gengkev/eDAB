"use strict";

angular.module("eDAB-utils", [])
	.config(function($provide, $httpProvider) {
		// display angularjs exceptions
		$provide.decorator("$exceptionHandler", function($delegate) {
			var func = function(exception, cause) {
				$delegate(exception, cause);
				func.errorMsg = (exception.name || exception) + ": " + exception.message;
			};
			func.errorMsg = null;
			return func;
		});
	})
	// for displaying error message
	.directive("displayError", function() {
		return {
			restrict: 'E',
			scope: true,
			template: '<div class="alert alert-error" ng-show="getErrorMsg()">' +'<b>Error:</b> {{getErrorMsg()}}<br/>' +
				'<small>More info available in the console. Contact developer for assistance.</small>' +
				'</div>'
		};
	})
	.service('Utils', function($window) {
		this.loadGPlusBadge = function() {
			if ($window.gapi) {
				$window.gapi.page.go('aboutEdab');
				$window.gapi.person.go('aboutEdab');
			}
		};
	})
	.run(function($window, Utils) {
		// uhh kind of complicated
		// we'll fix it eventually with a real loader
		var loadScript = (function(d, t) {
			return function(url) {
				var g = d.createElement(t),
					s = d.getElementsByTagName(t)[0];
				g.src = url;
				s.parentNode.insertBefore(g, s);
				return g;
			};
		}($window.document, 'script'));
		
		// Google Analytics snippet
		$window._gaq = [['_setAccount', 'UA-19517403-4'], ['_trackPageview']];
		loadScript('//www.google-analytics.com/ga.js');

		// Google+ badge setup
		$window.___gcfg = {
			lang: 'en-US',
			parsetags: 'explicit'
		};
		
		loadScript('https://apis.google.com/js/plusone.js').onload = function () {
			Utils.loadGPlusBadge();
		};
	});