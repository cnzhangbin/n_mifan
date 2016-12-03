"use strict"

Mifan.controller "404Ctrl1", ($scope) ->

  $scope.$on "$viewContentLoaded", -> $scope.$emit "pageChange", "404"
	  

  no