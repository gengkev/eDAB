function CourseListCtrl($scope, $location, $http, appService) {
	$http({
		method: "GET",
		url: "/rest/courses"
	})
	.then(function(response) {
		console.log("Loaded course list: ", response);
		$scope.courses = response.data;
	}, function(response) {
		// ...
		appService._reqHandler.error(response);
	});	
	
	$scope.newCourse = function() {
		$http({
			method: "POST",
			url: "/rest/courses",
			data: ""
		})
		.then(function(response) {
			var id = response.data.id;
			$location.path("/courses/" + id + "/edit");
		}, function(response) {
			// idk
			appService._reqHandler.error(response);
		});
	};
	
	$scope.getColor = function(index) {
		return "hsl(" + ((40 * index) % 255) + ", 56%, 83%)";
	};
}

function CourseCtrl($scope, $http, $routeParams, $location, appService) {
	var courseId = $routeParams.courseId;
	$http({
		method: "GET",
		url: "/rest/courses/" + courseId
	})
	.then(function(response) {
		console.log("Loaded course: ", response);
		$scope.course = response.data;
	}, function(response) {
		// ...
		appService._reqHandler.error(response);
	});
	
	$scope.edit = function() {
		$location.path("/courses/" + courseId + "/edit");
	};
}

function CourseEditCtrl($scope, $http, $routeParams, $location, appService) {
	var courseId = $routeParams.courseId;
	$http({
		method: "GET",
		url: "/rest/courses/" + courseId
	})
	.then(function(response) {
		console.log("Loaded course: ", response);
		$scope.course = response.data;
		$scope.cleanCourse = angular.copy($scope.course);
	}, function(response) {
		appService._reqHandler.error(response);
	});
	
	$scope.isUnchanged = function() {
		return angular.equals($scope.course, $scope.cleanCourse);
	};
	
	$scope.save = function() {
		$scope.disableButtons = true;
		
		var newCourse = angular.copy($scope.course);
		$http({
			method: "PUT",
			url: "/rest/courses/" + courseId,
			data: newCourse
		})
		.then(function(response) {
			console.log("Saved course: ", response);
			
			$scope.disableButtons = false;
			$scope.cleanCourse = newCourse;
			
			$scope.cancel();
		}, function(response) {
			$scope.disableButtons = false;
			appService._reqHandler.error(response);
		});
	};
	$scope.cancel = function() {
		$location.path("/courses/" + courseId);
	};
}