function CourseListCtrl($scope, $location, $http, appService) {
	$http({
		method: "GET",
		url: "/rest/courses"
	})
	.then(function(response) {
		console.log("Loaded course list: ", response);
		$scope.courses = response.data;
	}, appService._reqHandler.error);	
	
	$scope.newCourse = function() {
		$http({
			method: "POST",
			url: "/rest/courses",
			data: ""
		})
		.then(function(response) {
			var id = response.data.id;
			$location.path("/courses/" + id + "/edit");
		}, appService._reqHandler.error);
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
	}, appService._reqHandler.error);
	
	$scope.edit = function() {
		$location.path("/courses/" + courseId + "/edit");
	};
	
	$scope.addHomework = function() {
		$http({
			method: "POST",
			url: "/rest/courses/" + courseId + "/assignments",
			data: ""
		})
		.then(function(response) {
			var id = response.data.id;
			$location.path("/courses/" + courseId + "/assignments/" + id + "");
		}, appService._reqHandler.error);
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
		$scope.dirtyCourse = angular.copy($scope.course);
	}, appService._reqHandler.error);
	
	$scope.isUnchanged = function() {
		return angular.equals($scope.course, $scope.dirtyCourse);
	};
	
	$scope.save = function() {
		$scope.disableButtons = true;
		
		var newCourse = angular.copy($scope.dirtyCourse);
		$http({
			method: "PUT",
			url: "/rest/courses/" + courseId,
			data: newCourse
		})
		.then(function(response) {
			console.log("Saved course: ", response);
			
			$scope.disableButtons = false;
			$scope.course = newCourse;
			
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