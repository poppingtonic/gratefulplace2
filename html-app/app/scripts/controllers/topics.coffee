'use strict'

angular.module('gratefulplaceApp').controller 'TopicsCtrl', ($scope, Topic) ->
  $scope.$on 'topic.created', (e, topic)->
    $scope.topics.unshift topic
  
  Topic.query (data)->
    $scope.topics = data

  $scope.firstPost = (topic)->
    topic.posts[0]

  $scope.formatPostCount = (postCount)->
    switch postCount
      when 1 then "no replies"
      when 2 then "1 reply"
      else "#{postCount} replies"