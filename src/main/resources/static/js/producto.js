const form=document.getElementById('personalizar');
function update(){const extra=[...form.querySelectorAll('[data-precio]:checked')].reduce((sum,x)=>sum+Number(x.dataset.precio),0);document.getElementById('precio').textContent=((Number(form.dataset.base)+extra)*Number(form.cantidad.value)).toFixed(2);}
form.addEventListener('input',update);update();
