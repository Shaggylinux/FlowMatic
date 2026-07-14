function llenarModal(btn){
    document.getElementById("edit-id").value =
        btn.getAttribute("data-id");
    document.getElementById("edit-username").value =
        btn.getAttribute("data-username");
    document.getElementById("edit-apellido").value =
        btn.getAttribute("data-apellido");
    document.getElementById("edit-email").value =
        btn.getAttribute("data-email");
}

function pfCambiarFilas(select){
    var base = select.getAttribute("data-base");
    if(base){
        window.location.href =
            base + "?page=0&size=" + select.value;
    }
}



function pfIrAPagina(input){
    var p = parseInt(input.value);
    var max = parseInt(input.getAttribute("data-max"));
    if(!isNaN(p) && p>=1 && p<=max){
        var base = input.getAttribute("data-base");
        var size = input.getAttribute("data-size");
        window.location.href =
            base + "?page=" + (p-1) + "&size=" + size;
    }

}