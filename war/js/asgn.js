function AssignmentCtrl($scope, $http, $routeParams, $location, appService) {
	var courseId = $routeParams.courseId,
	    asgnId = $routeParams.asgnId;
	
	$http({
		method: "GET",
		url: "/rest/courses/" + courseId + "/assignments/" + asgnId
	})
	.then(function(response) {
		console.log("Loaded assignment: ", response);
		$scope.asgn = response.data;
	}, appService._reqHandler.error);
}