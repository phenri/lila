$(function() {

  var $frames = $("#frames");

  function onResize() {
    $frames.height(window.innerHeight + "px");
  }

  $(window).resize(onResize);
});
