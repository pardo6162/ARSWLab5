var app = (function () {

    class Point{
        constructor(x,y){
            this.x=x;
            this.y=y;
        }        
    }

    var idDraw=0;
    
    var stompClient = null;

    var addPolygonToCanvas = function (polygon){
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.fillStyle ='#f00'
        ctx.beginPath();
        var count = 0
        for(var  i=0; i<polygon.length;i++){
            var point= polygon[i]
            if (count==0){
                ctx.moveTo(point.x,point.y);
                count+=1;
            }else{
                ctx.lineTo(point.x,point.y);
            }
        }
        ctx.closePath();
        ctx.fill();
    }

    var addPointToCanvas = function (point) {
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
        ctx.stroke();

    };
    

    var getMousePosition = function (evt) {
        canvas = document.getElementById("canvas");
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left,
            y: evt.clientY - rect.top
        };
    };


    var connectAndSubscribe = function (idD) {
        idDraw=idD;
        console.info('Connecting to WS...');
        var socket = new SockJS('/stompendpoint');
        stompClient = Stomp.over(socket);
        
        //subscribe to /topic/TOPICXX when connections succeed
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/newpoint.'+idD, function (event) {
                var jsonEvent = JSON.parse(event.body);
                addPointToCanvas(jsonEvent);
            });
            stompClient.subscribe('/topic/newpolygon.'+idD,function (event){
                var jsonEvent = JSON.parse(event.body);
                addPolygonToCanvas(jsonEvent);
            });
        });


    };
    
    

    return {

        init: function (idD) {
            var can = document.getElementById("canvas");
            //websocket connection
            connectAndSubscribe(idD);
        },

        publishPoint: function(event){
            if(stompClient!= null){
                var point = getMousePosition(event);
                var px= point.x;
                var py= point.y;
                var pt=new Point(px,py);
                console.info("publishing point at "+pt);
                //publicar el evento
                stompClient.send("/app/newpoint."+idDraw,{},JSON.stringify(pt));
            }

        },

        disconnect: function () {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }
    };

})();