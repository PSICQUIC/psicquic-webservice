YAHOO.namespace("mpsq");

YAHOO.mpsq.status = {
  
    init: function( o ){
        var mode = o.mode;
        var anchor = o.anchor;
        
        if(mode === "index"){
            YAHOO.mpsq.status.initIndex( {"anchor":anchor} );
        }
        if(mode === "store"){
            YAHOO.mpsq.status.initStore( {"anchor":anchor} );
        }

    },

    initIndex: function( o ){

        var indexCallback = { cache:false, timeout: 15000,
                              success:YAHOO.mpsq.status.buildStatus,
                              failure:YAHOO.mpsq.status.asyncFailure,
                              argument:{ mode:"store", anchor:o.anchor } }; 
        
        var url = "mpsqmgr?mode=index&op.meta=meta";
        
        YAHOO.util.Connect.asyncRequest( 'GET', url, 
                                         indexCallback );
        
    },

    initStore: function( o ){

        var storeCallback = { cache:false, timeout: 15000,
                              success:YAHOO.mpsq.status.buildStatus,
                              failure:YAHOO.mpsq.status.asyncFailure,
                              argument:{ mode:"store", anchor:o.anchor } }; 
        
        var url = "mpsqmgr?mode=store&op.meta=meta";
        
        YAHOO.util.Connect.asyncRequest( 'GET', url, 
                                         storeCallback );
        
    },

    buildStatus: function( o ){
        
        var messages = YAHOO.lang.JSON.parse( o.responseText );
        var mode = o.argument.mode;
        var aid = o.argument.anchor;
        
        //alert(o.responseText);
        var meta = messages.meta;

        var rdiv = YAHOO.util.Dom.get( aid );
        
        var classHdElem = document.createElement('div');
        YAHOO.util.Dom.addClass( classHdElem, "prop" ); 
        classHdElem.innerHTML = "Resource Class";
        rdiv.appendChild( classHdElem );

        var classElem = document.createElement('div');
        YAHOO.util.Dom.addClass( classElem, "prop-value" ); 
        classElem.innerHTML = meta["resource-class"];
        rdiv.appendChild( classElem );

        var typeHdElem = document.createElement('div');
        YAHOO.util.Dom.addClass( typeHdElem, "prop" ); 
        typeHdElem.innerHTML = "Resource Type";
        rdiv.appendChild( typeHdElem );

        var typeElem = document.createElement('div');
        YAHOO.util.Dom.addClass( typeElem, "prop-value" ); 
        typeElem.innerHTML = meta["resource-type"];
        rdiv.appendChild( typeElem );

        var countsHdElem = document.createElement('div');
        YAHOO.util.Dom.addClass( countsHdElem, "prop" ); 
        countsHdElem.innerHTML = "Record Count";
        rdiv.appendChild( countsHdElem );
   
        var countLstElem = document.createElement('div');        
        rdiv.appendChild( countLstElem );
        var rt;
       
        for( rt in meta.counts ){
            if( rt !== "all" ){
               
                var rcount = meta.counts[rt];
            
                var rtElem = document.createElement('div');
                YAHOO.util.Dom.addClass( rtElem, "prop-key-val" ); 
                rtElem.innerHTML = 
                    "<div class='prop-key'>" + rt + "</div>" 
                    + "<div class='prop-val'>" + rcount + "</div>";
                countLstElem.appendChild( rtElem );
            }
        }

        if( meta.counts["all"] !== undefined ){
            var rcount = meta.counts["all"];
            
            var rtElem = document.createElement('dev');
            YAHOO.util.Dom.addClass( rtElem, "prop-key-val" ); 
            rtElem.innerHTML = 
                "<div class='prop-key'>Total</div>" 
                + "<div class='prop-val'>" + rcount + "</div>";
             countLstElem.appendChild( rtElem );
            
        }

        //var fooNode = document.createTextNode(cdate);  
      
    },

    asyncFailure: function( o ){
        alert( o );
    }
};