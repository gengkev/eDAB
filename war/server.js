angular.module('server', [])
	.service('server', function($window, $http, $rootScope, $location) {
		const RPC_URL = "/service";
		
		var _this = this;
		_this.auth = {
			login: function() {
				/*$window.location.replace("/login");*/
				$window.open(
					"login?close",
					"login",
					"menubar=no,toolbar=no,screenX=100,screenY=100,width=640,height=480"
				);
				$window.loginCallback = function() {
					_this.auth.check();
				};
			},
			logout: function() {
				$http({
					method: "POST",
					url: "/logout"
				})
				.then(function(resp) {
					_this.auth.check();
				});
			},
			logged_in: null,
			check: function () {
				//_this.auth.logged_in = false;
				_this.rpc._request("getCurrentUser", null)
					.then(function(resp) {
						if (resp.data && resp.data.result && !resp.data.error) {
							console.log(resp);
							_this.auth.logged_in = true;
							_this.user.name = resp.data.result.name;
							_this.user.fcps_id = resp.data.result.fcps_id;
							
							$location.path("/settings");
						} else {
							console.error(resp);
							_this.auth.logged_in = false;
							try {
								$rootScope.showError(JSON.stringify(resp.data.error));
							} catch(e) {}
						}
					});
			}
		};
		_this.user = {
			name: null,
			fcps_id: null
		};
		_this.rpc = {
			_counter: 0,
			_request: function(method, params) {
				return $http({
					method: "POST",
					url: RPC_URL,
					data: {
						"jsonrpc": "2.0",
						"method": method,
						"params": params,
						"id": _this.rpc._counter++
					}
				});
			}
		};
	})
	.run(function(server) {
		server.auth.check();
	});
	