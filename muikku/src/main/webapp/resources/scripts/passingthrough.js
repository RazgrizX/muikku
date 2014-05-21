$(document).ready(function() {

	// Dynamic navigation
	
  if ($('.wi-dock-dynami-navi-contentBgr').length > 0) {
    // ToDo: Calculate naviContainer height based on scroll event's scrollTop value
    var height = $(window).height();
    var naviContent = $('.wi-dock-dynami-navi-contentBgr');
    var tOffset = naviContent.offset();
    naviContent.height(height - tOffset.top);

    $(window).resize(function(){
      height = $(window).height();
      naviContent.height(height - tOffset.top);
    });
  }

	$('#dynamicNaviContainer').hide();
    	  
	$("a[class*='icon-dynamic-navi']").click(function() {
    var container = $('#dynamicNaviContainer');
      if ($('#dynamicNaviContainer:hidden').length !== 0) {
      container
      .show()
      .animate({
        "margin-left" : "0",
        opacity : 1
      }, 100, "easeOutSine");

    } else {
      container
      .animate({
        "margin-left" : "-300px",
        opacity : 0
      }, {
        duration : 100,
        easing : "easeOutSine",
        complete : function() {
          container.hide();
        }
      });
    }
  });

    // Widget settings tool area -->
	$( "div[class*='wi-frontpage']" ).mouseenter(function() {
		var tools =  $(this).find(".wi-frontpage-widget-toolarea");
	    tools.show( "slide", 50 );
	});
  
    $( "div[class*='wi-frontpage']" ).mouseleave(function() {
    	var tools =  $(this).find(".wi-frontpage-widget-toolarea");
        tools.hide( "slide", 50 );
    });
  
    // Widget dragging --> 
    
    $( "div[class*='wi-frontpage-dynamic']" ).draggable({ snapMode: "inner" });
        
    // Seeker functionalities -->
      
    $( "#seeker" ).focus(function() {
    	$('#seeker').val('');
    });

    $( "#seeker" ).blur(function() {
    	var sval = this.defaultValue;
    	$('#seeker').val(sval);
    });

    // fastlinks and dialogs for dock applications -->
    
    $("a[class*='icon-search']").click(function(){
      if ($("#seeker").data('blurring') == 'true') {
        return
      };
      
    	if ($(".wi-dock-search:hidden").length !== 0) {
        $(".wi-dock-search")
        .show()
        .clearQueue()
        .stop()
        .animate({
          width: '300px',
          opacity: 1,
         }, {
           duration : 100,
           easing : "easeInOutQuad",
           complete : function(){
             $(this).show();
             $("#seeker").focus();
             $("#seeker").blur(function(){
               $("#seeker").data('blurring', 'true');
               // Lets hide search tooltip as well.
               $(".dock-navi-tt-container-search")
               .clearQueue()
               .finish()
               .css({
                 height: '0px',
                 left: '0px',
                 opacity: 0
               })
               .hide();
               $(".wi-dock-search")
               .clearQueue()
               .stop()
               .animate({
                 width: 0,
                 opacity: 0
               }, {
                 duration : 100,
                 easing : "easeInOutQuad",
                 complete : function(){
                   $("#seeker").data('blurring', 'false');
                   $(this).hide();
                 }
               });
             });
           }
         });
    	} else {
        $(".wi-dock-search")
        .show()
        .clearQueue()
        .stop()
        .animate({
          width: '0px',
          opacity: 0,
        }, {
          duration : 100,
          easing : "easeInOutQuad",
          complete: function () {
            $(this).hide();
          }
        });
    	}
    });
    
    // Tooltip 
    
    $("div[class*='wi-dock-static-navi']").mouseenter(function() {
      var tooltip = $(this).children("[class*='dock-navi-tt-container']");
      var innerTooltip = tooltip.children('div');
      
      // Lets prevent search results from disappearing
      if ($(".wi-dock-search:hidden").length !== 0) {
        tooltip.show();
        
        // Offsets
        
        var iconOffset = tooltip.parent().offset().left + 15;
        var tOffset = tooltip.offset().left;
        var paddingOffset = tOffset + 20;
                  
        // Sets tooltip's inner elements padding-left same as tooltip's left position 
        // and sets background-position relative to icons position
        
        innerTooltip.css({
          paddingLeft: paddingOffset + 'px',
          backgroundPosition: iconOffset + 'px 0px'
        });
        
        // Sets tooltip's width same as view port width 
        // and left position is adjusted accordingly
        
        tooltip.css({
          width: $(window).width(),
          left:-tOffset + 'px'
        });     
        
        tooltip
        .animate({
        	height: '300px',
        	opacity: 1
        }, {
        	duration : 200,
        	easing: "easeInOutQuad",
        	complete : function(){
            // This is for added functions. In the future. Of mankind.
        	}      
        });
      }

    });

    $("div[class*='wi-dock-static-navi']").mouseleave(function() {
      var tooltip =  $(this).find("[class*='dock-navi-tt-container']");
    	
      // Lets prevent search results from disappearing
    	if ($(".wi-dock-search:hidden").length !== 0) {
      	tooltip
      	.clearQueue()
      	.finish()
      	.css({
      	  height: '0px',
      	  left: '0px',
      	  opacity: 0
      	})
      	.hide();
    }
    	
    });
});



