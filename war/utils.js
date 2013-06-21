"use strict";

angular.module("eDAB-utils", [])
    // hacks to make fading in/out work for login dialog
    .directive("fadeInOut", function ($timeout) {
		return function (scope, element, attrs) {
			var doShow = true, isShowing = true,
			    timeout = parseInt(attrs.timeout, 10) || 2000,
			    timeoutId;
			
			function show() {
				if (timeoutId) { $timeout.cancel(timeoutId); }
				
				element[0].style.transition = (timeout / 1000) + "s ease";
				element[0].style.display = "block";
				
				$timeout(function () {
					element[0].style.opacity = "1";
				}, 0);
			}
			function hide() {
				if (timeoutId) { $timeout.cancel(timeoutId); }
				
				element[0].style.transition = (timeout / 1000) + "s ease";
				element[0].style.opacity = "0";
				
				timeoutId = $timeout(function () {
					timeoutId = null;
					element[0].style.display = "none";
				}, timeout);
			}
			
			scope.$watch(attrs.fadeInOut, function (value) {
				var newDoShow = !!value;
				// console.log("newDoShow", newDoShow);
				if (newDoShow !== doShow) {
					if (newDoShow) { show(); }
					else { hide(); }
					
					doShow = newDoShow;
				}
			});
		};
	})
	.config(function($provide, $httpProvider) {
		// display angularjs exceptions
		$provide.decorator("$exceptionHandler", function($delegate) {
			var func = function(exception, cause) {
				$delegate(exception, cause);
				func.errorMsg = exception.name + ": " + exception.message;
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
            template: '<div class="alert alert-error" ng-show="getErrorMsg()">'
                + '<b>Error:</b> {{getErrorMsg()}}<br/>'
                + '<small>More info available in the console. Contact developer for assistance.</small>'
                + '</div>'
        };
    })
    .run(function($window, $rootScope, $exceptionHandler) {
        // Google Analytics snippet
		$window._gaq = [['_setAccount', 'UA-19517403-4'], ['_trackPageview']];
		(function(d, t) {
			var g = d.createElement(t),
			    s = d.getElementsByTagName(t)[0];
			g.src = '//www.google-analytics.com/ga.js';
			s.parentNode.insertBefore(g, s);
		}($window.document, 'script'));
	});