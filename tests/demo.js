
'use strict';const names=['Legado de oro','Jardín de lirios','Retrato editorial','Luz eterna','Cielo sereno','Magnolia de paz','Lazo de memoria','Siempre contigo','Retrato de luz','Cielo eterno','Marfil de recuerdos','Jardín nocturno'];const themes=[['#13191f','#d9b786','#f6f3ed'],['#f7f0e5','#9d835c','#292b34'],['#1c2634','#e3c58c','#faf6ed'],['#eee6df','#af8368','#273342'],['#203b43','#d8c9a7','#fffdf8'],['#fbf7f0','#b69a72','#34323b'],['#46313b','#ebd4bb','#fff9f5'],['#e5dbcd','#9e7551','#242735'],['#171719','#e2bb77','#fff5e6'],['#17212b','#ecd19a','#fff6e8'],['#f8f2e6','#ad854e','#35302e'],['#211919','#dfba8c','#fff8ed']];let variant=0,story=false,img=null;const $=id=>document.getElementById(id),canvas=$('mainCanvas');
function rr(g,x,y,w,h,r){g.beginPath();g.roundRect(x,y,w,h,r);}
function ellipse(g,x,y,w,h){g.beginPath();g.ellipse(x,y,w/2,h/2,0,0,Math.PI*2)}
function flower(g,x,y,r,accent){g.save();g.translate(x,y);for(let i=0;i<6;i++){g.save();g.rotate(i*Math.PI/3);g.beginPath();g.ellipse(0,-r*.55,r*.27,r*.63,0,0,Math.PI*2);g.fillStyle=i%2?'#e3dacb':'#fffdf6';g.fill();g.strokeStyle=accent;g.lineWidth=2;g.stroke();g.restore()}g.fillStyle=accent;g.beginPath();g.arc(0,0,r*.17,0,Math.PI*2);g.fill();g.restore()}
function ribbon(g,x,y,s,accent){g.save();g.translate(x,y);g.scale(s/190,s/190);g.shadowColor=accent;g.shadowBlur=20;g.strokeStyle=accent;g.lineWidth=8;g.fillStyle='#141418';g.beginPath();g.moveTo(-45,-100);g.bezierCurveTo(0,-175,113,-105,70,-10);g.bezierCurveTo(32,80,-62,121,-85,160);g.lineTo(-120,120);g.bezierCurveTo(-30,26,55,-55,22,-88);g.bezierCurveTo(-12,-118,-52,-75,-53,-40);g.bezierCurveTo(-50,17,43,88,110,125);g.lineTo(79,167);g.bezierCurveTo(-45,80,-111,10,-104,-55);g.bezierCurveTo(-100,-88,-73,-106,-45,-100);g.closePath();g.fill();g.stroke();g.restore()}
function lines(g,str,x,y,maxWidth,fontSize,color,maxRows=3){g.fillStyle=color;g.textAlign='center';g.font=`${fontSize}px Georgia,serif`;let words=(str||'').trim().split(/\s+/),out=[],current='';for(let word of words){let candidate=current?current+' '+word:word;if(g.measureText(candidate).width>maxWidth&&current){out.push(current);current=word}else current=candidate}if(current)out.push(current);if(out.length>maxRows){out=out.slice(0,maxRows);let last=out[maxRows-1];while(last.length>1&&g.measureText(last+'…').width>maxWidth)last=last.slice(0,-1);out[maxRows-1]=last+'…'}let dy=fontSize*1.2;out.forEach((txt,i)=>g.fillText(txt,x,y+(i-(out.length-1)/2)*dy,maxWidth))}
function small(g,str,x,y,color){g.fillStyle=color;g.textAlign='center';g.font='bold 27px system-ui';g.fillText(str,x,y,860)}
function drawPhoto(g,x,y,w,h,r,oval,accent){g.save();if(oval)ellipse(g,x,y,w,h);else rr(g,x-w/2,y-h/2,w,h,r);g.save();g.shadowColor=accent;g.shadowBlur=16;g.strokeStyle=accent;g.lineWidth=9;g.stroke();g.restore();g.clip();if(img){const zoom=Number($('zoom').value)/100;const scale=Math.max(w/img.width,h/img.height)*zoom;const dw=img.width*scale,dh=img.height*scale;const mx=Math.max(0,(dw-w)/2)*Number($('panX').value)/100;const my=Math.max(0,(dh-h)/2)*Number($('panY').value)/100;g.drawImage(img,x-dw/2+mx,y-dh/2+my,dw,dh)}else{let bg=g.createLinearGradient(x,y-h/2,x,y+h/2);bg.addColorStop(0,'#ddd7cd');bg.addColorStop(1,'#67707a');g.fillStyle=bg;g.fillRect(x-w/2,y-h/2,w,h);g.fillStyle='#ffffffb5';g.beginPath();g.arc(x,y-35,80,0,Math.PI*2);g.fill();g.beginPath();g.ellipse(x,y+210,170,175,0,0,Math.PI*2);g.fill();small(g,'AÑADIR FOTO',x,y+h/2-45,'#fff')}g.restore()}
function petal(g,x,y,r,angle,accent){g.save();g.translate(x,y);g.rotate(angle);g.fillStyle='#fefcf4';g.strokeStyle=accent;g.lineWidth=1.2;g.beginPath();g.moveTo(0,0);g.bezierCurveTo(-r*.38,-r*.20,-r*.49,-r*.80,0,-r);g.bezierCurveTo(r*.50,-r*.81,r*.4,-r*.16,0,0);g.closePath();g.fill();g.stroke();g.restore()}
function lily(g,x,y,r,accent){g.save();g.translate(x,y);g.shadowColor='#0004';g.shadowBlur=10;for(let i=0;i<6;i++)petal(g,0,0,r,i*Math.PI/3,accent);g.shadowBlur=0;g.strokeStyle='#8b764b';g.lineWidth=2.4;for(let i=0;i<5;i++){let a=i*Math.PI*2/5;g.beginPath();g.moveTo(0,0);g.lineTo(Math.sin(a)*r*.37,Math.cos(a)*r*.37);g.stroke();g.fillStyle='#b68b3e';g.beginPath();g.ellipse(Math.sin(a)*r*.4,Math.cos(a)*r*.4,5,9,a,0,Math.PI*2);g.fill()}g.restore()}
function leaf(g,x,y,r,rot){g.save();g.translate(x,y);g.rotate(rot);g.fillStyle='#53644c';g.beginPath();g.moveTo(0,0);g.quadraticCurveTo(r*.38,-r*.84,0,-r*1.5);g.quadraticCurveTo(-r*.42,-r*.91,0,0);g.fill();g.strokeStyle='#c1c99c';g.lineWidth=1.2;g.beginPath();g.moveTo(0,0);g.lineTo(0,-r*1.37);g.stroke();g.restore()}
function floral(g,x,y,r,accent,flipped=false){g.save();g.translate(x,y);if(flipped)g.scale(-1,1);leaf(g,-r*.64,r*.45,r*.72,-.85);leaf(g,r*.65,r*.32,r*.6,.88);leaf(g,-r*.15,r*.5,r*.49,-.45);lily(g,0,0,r,accent);g.restore()}
function corner(g,W,H,color){g.save();g.strokeStyle=color;g.globalAlpha=.65;g.lineWidth=2;for(let k=0;k<4;k++){g.save();g.translate(k%2?W-84:84,k>1?H-84:84);g.rotate(k===0?0:k===1?Math.PI/2:k===2?-Math.PI/2:Math.PI);g.beginPath();g.moveTo(0,95);g.lineTo(0,30);g.quadraticCurveTo(0,0,30,0);g.lineTo(95,0);g.stroke();for(let j=0;j<3;j++){g.beginPath();g.ellipse(14+j*21,14+j*16,11,4,-.7,0,2*Math.PI);g.stroke()}g.restore()}g.restore()}
function candle(g,x,y,s){g.save();g.translate(x,y);g.scale(s,s);let grad=g.createLinearGradient(-35,0,35,0);grad.addColorStop(0,'#9c7955');grad.addColorStop(.35,'#fff4dc');grad.addColorStop(.7,'#f9e7c3');grad.addColorStop(1,'#7c5c47');g.fillStyle=grad;rr(g,-34,10,68,118,8);g.fill();g.shadowColor='#ffd78b';g.shadowBlur=36;g.fillStyle='#ffe29b';g.beginPath();g.moveTo(0,-56);g.bezierCurveTo(-27,-17,-12,3,0,5);g.bezierCurveTo(17,-4,20,-19,0,-56);g.fill();g.shadowBlur=0;g.fillStyle='#fff9df';g.beginPath();g.ellipse(0,-9,9,20,0,0,2*Math.PI);g.fill();g.restore()}
function star(g,x,y,r,c){g.save();g.translate(x,y);g.strokeStyle=c;g.lineWidth=2;g.beginPath();g.moveTo(-r,0);g.lineTo(r,0);g.moveTo(0,-r*1.5);g.lineTo(0,r*1.5);g.stroke();g.restore()}
function makeBackground(g,W,H,v){let base=themes[v][0],light=[1,3,5,7,10].includes(v);let grad=g.createLinearGradient(0,0,W,H);if(light){grad.addColorStop(0,base);grad.addColorStop(.6,v===3?'#e7dbcf':'#e6dcd0');grad.addColorStop(1,'#cbbcac')}else{grad.addColorStop(0,base);grad.addColorStop(.62,v===6?'#281f28':'#18212b');grad.addColorStop(1,'#08090d')}g.fillStyle=grad;g.fillRect(0,0,W,H);let halo=g.createRadialGradient(W*.7,H*.2,20,W*.6,H*.22,820);halo.addColorStop(0,light?'#fffcf85b':'#e4bc7c36');halo.addColorStop(1,'#ffffff00');g.fillStyle=halo;g.fillRect(0,0,W,H);g.strokeStyle=themes[v][1];g.globalAlpha=.72;g.lineWidth=3;rr(g,44,44,W-88,H-88,15);g.stroke();g.globalAlpha=.35;g.lineWidth=1.2;rr(g,63,63,W-126,H-126,12);g.stroke();g.globalAlpha=1;corner(g,W,H,themes[v][1])}
function separator(g,y,color){g.save();g.strokeStyle=color;g.globalAlpha=.82;g.lineWidth=2;g.beginPath();g.moveTo(305,y);g.lineTo(478,y);g.moveTo(602,y);g.lineTo(775,y);g.stroke();star(g,540,y,9,color);g.restore()}
function cinematicVeil(g,x,y,w,h,light=false){
  // Editorial tonal fade: softens the bottom of the photograph, without manipulating faces.
  const veil=g.createLinearGradient(0,y-h*.27,0,y+h*.56);
  veil.addColorStop(0,'rgba(10,10,12,0)');
  veil.addColorStop(.57,light?'rgba(245,235,217,0.04)':'rgba(18,15,14,0.08)');
  veil.addColorStop(1,light?'rgba(246,238,224,0.98)':'rgba(18,16,18,0.98)');
  g.fillStyle=veil;g.fillRect(x-w/2-8,y-h*.30,w+16,h*.93);
}
function atmosphere(g,W,cy,v){
  const halo=g.createRadialGradient(W*.52,cy-260,20,W*.52,cy-240,580);
  halo.addColorStop(0,v===10?'rgba(250,215,163,.28)':'rgba(248,202,124,.25)');
  halo.addColorStop(.62,'rgba(235,177,112,.08)');halo.addColorStop(1,'rgba(235,177,112,0)');
  g.fillStyle=halo;g.fillRect(0,cy-650,W,1050);
  for(let j=0;j<5;j++)star(g,135+j*191,cy-380+(j%3)*29,7+(j%2)*4,themes[v][1]);
}
function caption(g,mode,v,cy){let gold=themes[v][1],fg=themes[v][2],t=cy+(mode?535:410);g.textAlign='center';separator(g,t-57,gold);g.fillStyle=gold;g.font='600 27px system-ui';g.fillText(mode?'SIEMPRE EN MI CORAZÓN':'EN TU MEMORIA',540,t,840);let userName=$('name').value||'Un recuerdo eterno';lines(g,userName,540,t+96,870,53,fg,2);lines(g,$('phrase').value,540,t+190,870,33,fg,2);lines(g,$('message').value,540,t+292,840,28,fg,2);if(mode){separator(g,1750,gold);g.fillStyle=fg;g.font='24px Georgia,serif';g.fillText('Tu amor permanece en cada recuerdo',540,1818,850)}}
function paint(g,mode=story,v=variant){const W=1080,H=mode?1920:1350,gold=themes[v][1],cy=mode?715:500;g.clearRect(0,0,W,H);makeBackground(g,W,H,v);
if(v===0){drawPhoto(g,540,cy-5,568,644,20,false,gold);floral(g,792,cy+265,98,gold);ribbon(g,841,cy+208,71,gold)}
if(v===1){drawPhoto(g,540,cy,555,660,0,true,gold);floral(g,253,cy-295,104,gold);floral(g,826,cy+258,100,gold,true)}
if(v===2){drawPhoto(g,540,cy-13,740,572,16,false,gold);g.strokeStyle=gold;g.lineWidth=3;g.strokeRect(158,cy-315,764,608);star(g,540,cy-344,14,gold)}
if(v===3){drawPhoto(g,408,cy-5,585,695,22,false,gold);ribbon(g,835,cy-50,147,gold);candle(g,859,cy+190,.75)}
if(v===4){drawPhoto(g,540,cy-15,606,600,0,true,gold);star(g,260,cy-230,22,gold);floral(g,824,cy+255,88,gold,true)}
if(v===5){drawPhoto(g,540,cy-48,832,517,96,false,gold);floral(g,230,cy+245,96,gold);floral(g,844,cy+245,87,gold,true)}
if(v===6){drawPhoto(g,540,cy,660,720,23,false,gold);ribbon(g,836,cy-252,104,gold);floral(g,242,cy+248,76,gold)}
if(v===7){drawPhoto(g,540,cy-12,614,682,118,false,gold);floral(g,232,cy+246,91,gold);candle(g,865,cy+191,.52)}
if(v===8){atmosphere(g,W,cy,v);drawPhoto(g,540,cy-32,842,690,22,false,gold);cinematicVeil(g,540,cy,850,700);floral(g,140,cy+250,116,gold);floral(g,948,cy+255,111,gold,true);candle(g,926,cy+300,.62)}
if(v===9){atmosphere(g,W,cy,v);drawPhoto(g,540,cy-35,675,710,0,true,gold);cinematicVeil(g,540,cy+55,750,650);floral(g,202,cy+268,110,gold);floral(g,877,cy+266,106,gold,true);star(g,540,cy-415,25,gold)}
if(v===10){atmosphere(g,W,cy,v);drawPhoto(g,540,cy-43,850,665,45,false,gold);cinematicVeil(g,540,cy+16,860,625,true);floral(g,155,cy+252,120,gold);floral(g,930,cy+268,100,gold,true)}
if(v===11){atmosphere(g,W,cy,v);drawPhoto(g,540,cy-25,766,710,32,false,gold);cinematicVeil(g,540,cy+40,802,640);floral(g,178,cy+258,120,gold);floral(g,903,cy+247,105,gold,true);candle(g,881,cy+308,.47)}
caption(g,mode,v,cy)}

const readyPresets=[["Lazo eterno", "Hoy mi corazón está de luto", "Tu recuerdo vivirá siempre en mí."], ["Siempre contigo", "Siempre en nuestros corazones", "El amor nunca se despide."], ["Vuela alto", "Vuela alto", "Que la paz acompañe tu descanso."], ["Hasta siempre", "Hasta siempre", "Gracias por todos los momentos compartidos."], ["Una luz eterna", "Tu luz permanece", "Hay recuerdos que jamás se apagan."], ["Descansa en paz", "Descansa en paz", "Te recordaremos con amor."], ["Lirios de amor", "Con amor eterno", "Tu ausencia se siente, tu amor permanece."], ["Rosa blanca", "Una flor para tu recuerdo", "Siempre estarás presente."], ["Jardín de memoria", "Floreces en mis recuerdos", "Tu cariño vive en nosotros."], ["Amor inmenso", "El amor trasciende", "No hay distancia para los recuerdos."], ["Paz de marfil", "Un recuerdo lleno de paz", "Que descanses en serenidad."], ["Pétalos de luz", "Tu memoria florece", "Nunca olvidaremos tu sonrisa."], ["Vela encendida", "Una vela por ti", "Tu recuerdo ilumina nuestro camino."], ["Luz en la noche", "Siempre brillarás", "Ni el tiempo apagará tu luz."], ["Luz de esperanza", "La esperanza nos acompaña", "Que encuentres descanso eterno."], ["Eterna presencia", "Vives en nuestros pensamientos", "Gracias por tu amor incondicional."], ["Recuerdo dorado", "Un amor que permanece", "Cada recuerdo es un abrazo."], ["Noche serena", "En paz y en luz", "Te llevamos en el corazón."], ["Cielo de paz", "Que el cielo te abrace", "Tu recuerdo nos da fortaleza."], ["Paloma blanca", "Vuela en paz", "Siempre te recordaremos."], ["Camino de luz", "Hasta volver a encontrarnos", "Guardamos cada instante de amor."], ["Oración de paz", "Dale, Señor, el descanso eterno", "Y brille para ti la luz perpetua."], ["Fe y recuerdo", "En manos de Dios", "Conservamos tu memoria con amor."], ["Cielo luminoso", "Descanso y esperanza", "Que la paz te acompañe."], ["Te extraño", "Te extraño cada día", "Tu ausencia deja un silencio lleno de recuerdos."], ["Gracias por tanto", "Gracias por tanto amor", "Cada enseñanza tuya sigue conmigo."], ["Por siempre mamá", "Mamá, siempre contigo", "Tu amor seguirá guiándome."], ["Por siempre papá", "Papá, nunca te olvidaremos", "Tu ejemplo vive en cada paso."], ["Un aniversario más", "Hoy te recordamos", "El tiempo pasa, el amor permanece."], ["Amistad eterna", "Amigos para siempre", "Tu amistad será un recuerdo precioso."]];
const readyGroups=['Todos','Clásicos','Florales','Velas','Espirituales','Dedicatorias'];
let readyId=-1,readyFilter=0;
function readyPaint(g){const W=1080,H=1920;const group=Math.floor(readyId/6);const item=readyId%6;const variants=[0,1,3,4,6,8,9,10,11,2,5,7];const v=variants[(group*2+item)%variants.length];const gold=themes[v][1],fg=themes[v][2];makeBackground(g,W,H,v);if(group===0){ribbon(g,540,605,360,gold);floral(g,792,760,135,gold);star(g,245,340,20,gold)}
else if(group===1){ribbon(g,540,650,240,gold);floral(g,245,540,145,gold);floral(g,826,805,145,gold,true)}
else if(group===2){ribbon(g,540,625,285,gold);candle(g,300,795,1.32);candle(g,770,800,1.12);floral(g,832,975,105,gold,true)}
else if(group===3){const a=g.createRadialGradient(540,450,5,540,550,650);a.addColorStop(0,'#ddc28e90');a.addColorStop(1,'#d7bd8900');g.fillStyle=a;g.fillRect(0,0,W,1300);star(g,540,365,40,gold);ribbon(g,540,730,230,gold);floral(g,820,860,100,gold)}
else {ribbon(g,540,585,280,gold);floral(g,222,770,125,gold);floral(g,850,790,110,gold,true)}
separator(g,1110,gold);small(g,'UN RECUERDO ESPECIAL',540,1170,gold);lines(g,$('phrase').value,540,1310,870,65,fg,3);separator(g,1440,gold);lines(g,$('message').value,540,1580,830,38,fg,4);const person=$('name').value.trim();if(person)lines(g,person,540,1760,840,35,fg,2)}
function readySelect(i){if(!Number.isInteger(i)||i<0||i>=readyPresets.length)return;readyId=i;story=true;$('cardMode').classList.remove('active');$('storyMode').classList.add('active');$('name').value='';$('phrase').value=readyPresets[i][1];$('message').value=readyPresets[i][2];$('designTitle').textContent=readyPresets[i][0]+' · Estado listo';personalTouch();document.querySelectorAll('.ready-grid button').forEach(x=>x.classList.toggle('active',Number(x.dataset.index)===i));render()}
function readyList(){const b=$('readyGrid');b.replaceChildren();readyPresets.forEach((p,i)=>{if(readyFilter && Math.floor(i/6)!==readyFilter-1)return;let el=document.createElement('button');el.type='button';el.dataset.index=i;el.innerHTML='<strong>'+p[0]+'</strong><small>'+p[1]+'</small>';el.classList.toggle('active',i===readyId);el.onclick=()=>readySelect(i);b.append(el)})}
readyGroups.forEach((label,i)=>{let b=document.createElement('button');b.textContent=label;b.type='button';b.classList.toggle('active',i===0);b.onclick=()=>{readyFilter=i;document.querySelectorAll('#readyFilters button').forEach((x,j)=>x.classList.toggle('active',j===i));readyList()};$('readyFilters').append(b)});readyList();
$('clearReady').onclick=()=>{readyId=-1;story=false;$('cardMode').classList.add('active');$('storyMode').classList.remove('active');render();readyList()};

// Fast preview: render high-resolution main canvas only; previews use a shared thumbnail surface.
function render(refreshThumbnails=false){
  canvas.height=story?1920:1350;
  const g=canvas.getContext('2d');if(!g){showMsg('Tu navegador no permite dibujar imágenes');return}
  if(readyId>=0)readyPaint(g);else paint(g);
  $('designTitle').textContent=readyId>=0?readyPresets[readyId][0]+' · Estado listo':names[variant]+(story?' · Estado':' · Homenaje');
  document.querySelectorAll('.tile').forEach((n,i)=>n.classList.toggle('selected',i===variant));
  if(refreshThumbnails)drawTiles();
}
function drawTiles(){
  document.querySelectorAll('.tile canvas').forEach((el,i)=>{
    const width=160,height=200;
    el.width=width;el.height=height;
    const ctx=el.getContext('2d');if(!ctx)return;
    const scale=Math.min(width/1080,height/(story?1920:1350));
    ctx.save();ctx.translate((width-1080*scale)/2,(height-(story?1920:1350)*scale)/2);
    ctx.scale(scale,scale);paint(ctx,story,i);ctx.restore();
  });
}
for(let i=0;i<12;i++){let el=document.createElement('button');el.className='tile';el.innerHTML='<canvas width="160" height="192"></canvas><span>'+names[i]+'</span>';el.onclick=()=>{readyId=-1;variant=i;readyList();personalTouch();render(true)};$('gallery').append(el)}
for(const id of ['name','phrase','message','zoom','panX','panY'])$(id).addEventListener('input',()=>{render();scheduleDraft()});
let photoSelectionVersion=0;
async function prepareWebPhoto(file){
  if(!file||(!file.type.startsWith('image/')&&!/\.(jpe?g|png|webp|heic|heif)$/i.test(file.name)))throw Error('Selecciona una fotografía válida');
  if(file.size>32*1024*1024)throw Error('La foto supera 32 MB. Selecciona una copia más pequeña.');
  const url=URL.createObjectURL(file);
  try{
    const raw=new Image();
    await new Promise((resolve,reject)=>{raw.onload=resolve;raw.onerror=()=>reject(Error('La imagen no se pudo abrir'));raw.src=url});
    if(!raw.naturalWidth||!raw.naturalHeight)throw Error('Fotografía vacía');
    const ratio=Math.min(1,2300/Math.max(raw.naturalWidth,raw.naturalHeight));
    const cv=document.createElement('canvas');cv.width=Math.max(1,Math.round(raw.naturalWidth*ratio));cv.height=Math.max(1,Math.round(raw.naturalHeight*ratio));
    const ctx=cv.getContext('2d');if(!ctx)throw Error('No se pudo procesar la fotografía');
    ctx.fillStyle='#ffffff';ctx.fillRect(0,0,cv.width,cv.height);ctx.drawImage(raw,0,0,cv.width,cv.height);
    const blob=await new Promise((resolve,reject)=>cv.toBlob(b=>b?resolve(b):reject(Error('No se pudo reducir la fotografía')),'image/jpeg',.91));
    return blob;
  }finally{URL.revokeObjectURL(url)}
}
async function imageFromBlob(blob){
  const url=URL.createObjectURL(blob);
  try{const picture=new Image();await new Promise((resolve,reject)=>{picture.onload=resolve;picture.onerror=()=>reject(Error('Imagen no disponible'));picture.src=url});return picture;}
  finally{URL.revokeObjectURL(url)}
}
$('photo').onchange=async e=>{
  const file=e.target.files?.[0];if(!file)return;
  const token=++photoSelectionVersion;projectLoadToken++;showMsg('Preparando fotografía…');
  try{
    const blob=await prepareWebPhoto(file);
    const picture=await imageFromBlob(blob);
    if(token!==photoSelectionVersion)return;
    img=picture;photoFile=blob;render(true);scheduleDraft();showMsg('Fotografía preparada y optimizada');
  }catch(error){if(token===photoSelectionVersion){clearTimeout(autoSaveTimer);showMsg(error.message||'No se pudo abrir la fotografía');alert(error.message||'Foto no válida')}}
};
$('remove').onclick=()=>{photoSelectionVersion++;projectLoadToken++;img=null;photoFile=null;$('photo').value='';render(true);scheduleDraft();};
$('reset').onclick=()=>{for(let id of ['zoom','panX','panY'])$(id).value=id==='zoom'?100:0;render();scheduleDraft()};
$('cardMode').onclick=()=>{readyId=-1;readyList();story=false;$('cardMode').classList.add('active');$('storyMode').classList.remove('active');render(true);personalTouch()};$('storyMode').onclick=()=>{readyId=-1;readyList();story=true;$('storyMode').classList.add('active');$('cardMode').classList.remove('active');render(true);personalTouch()};
$('share').onclick=async()=>{try{const blob=await new Promise(resolve=>canvas.toBlob(resolve,'image/png'));if(!blob)throw Error('No se pudo preparar PNG');const file=new File([blob],`recuerdo_${variant+1}.png`,{type:'image/png'});if(navigator.canShare?.({files:[file]}))await navigator.share({files:[file],title:'Un recuerdo especial'});else alert('El navegador no admite compartir archivos aquí. Utiliza Descargar PNG.')}catch(e){if(e.name!=='AbortError')alert('No se pudo compartir. Puedes descargar la imagen en PNG.')}};
$('download').onclick=async()=>{
  try{
    const blob=await new Promise((resolve,reject)=>canvas.toBlob(b=>b?resolve(b):reject(Error('No se pudo crear el PNG')),'image/png'));
    const url=URL.createObjectURL(blob),a=document.createElement('a');
    a.href=url;a.download=`Esquelas_LauraApps_${story?'Estado':'Homenaje'}_${variant+1}.png`;
    document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),15000);
    showMsg('Imagen PNG preparada para descargar');
  }catch(e){showMsg('Error al guardar imagen: '+e.message)}
};
// Paso 4: estado local offline. Las fotos se almacenan solo en IndexedDB del dispositivo.
const secureStorage=(()=>{
  const mem=new Map();let durable=false;
  try{const k='__esquelas_storage_probe';window.localStorage.setItem(k,'ok');window.localStorage.removeItem(k);durable=true;}catch(e){}
  return {
    get durable(){return durable},
    getItem:k=>{if(durable){try{return window.localStorage.getItem(k)}catch(e){durable=false}}return mem.get(k)||null},
    setItem:(k,v)=>{mem.set(k,v);if(!durable)return false;try{window.localStorage.setItem(k,v);return true}catch(e){durable=false;return false}}
  };
})();
const STORAGE='lauraapps_esquelas_v15';
function safeArray(key){try{const x=JSON.parse(secureStorage.getItem(key)||'[]');return Array.isArray(x)?x:[]}catch(e){return []}}
const favSet=new Set(safeArray(STORAGE+'_favorites'));
let recentIds=safeArray(STORAGE+'_recent');
let projects=safeArray(STORAGE+'_projects').filter(x=>x&&typeof x==='object'&&typeof x.key==='string'&&/^(ready-([0-9]|[12][0-9])|portrait-([0-9]|1[01]))$/.test(x.key)).slice(0,25);
let editingId=null,autoSaveTimer=null,photoFile=null,restoring=false;
let photoDbPromise;
function photoDb(){if(!photoDbPromise)photoDbPromise=new Promise((resolve,reject)=>{const r=indexedDB.open('esquelas_fotos_v1',1);r.onupgradeneeded=()=>r.result.createObjectStore('photos');r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error)});return photoDbPromise}
async function storePhoto(key,blob){const db=await photoDb();return new Promise((resolve,reject)=>{const tx=db.transaction('photos','readwrite');tx.objectStore('photos').put(blob,key);tx.oncomplete=()=>resolve();tx.onerror=()=>reject(tx.error)})}
async function getPhoto(key){const db=await photoDb();return new Promise((resolve,reject)=>{const tx=db.transaction('photos','readonly');const r=tx.objectStore('photos').get(key);r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error)})}
async function removePhoto(key){const db=await photoDb();return new Promise((resolve,reject)=>{const tx=db.transaction('photos','readwrite');tx.objectStore('photos').delete(key);tx.oncomplete=()=>resolve();tx.onerror=()=>reject(tx.error)})}
const designKey=()=>readyId>=0?'ready-'+readyId:'portrait-'+variant;
function persistMeta(){
  const a=secureStorage.setItem(STORAGE+'_favorites',JSON.stringify([...favSet]));
  const b=secureStorage.setItem(STORAGE+'_recent',JSON.stringify(recentIds));
  const c=secureStorage.setItem(STORAGE+'_projects',JSON.stringify(projects));
  return a&&b&&c;
}
function personalTouch(){if(restoring)return;const k=designKey();recentIds=[k,...recentIds.filter(x=>x!==k)].slice(0,12);persistMeta();updateFavorite();scheduleDraft()}
function updateFavorite(){$('favToggle').textContent=(favSet.has(designKey())?'♥ Quitar favorito':'♡ Añadir favorito')}
function snapshot(){return {key:designKey(),name:$('name').value,phrase:$('phrase').value,message:$('message').value,zoom:$('zoom').value,panX:$('panX').value,panY:$('panY').value,story,time:Date.now()}}
function writeDraft(){
  clearTimeout(autoSaveTimer);if(restoring)return;
  const ok=secureStorage.setItem(STORAGE+'_draft',JSON.stringify(snapshot()));
  $('saveStatus').textContent=ok?'Textos y encuadre guardados automáticamente':'Almacenamiento no disponible: usa Descargar PNG para no perder cambios';
}
function scheduleDraft(){if(restoring)return;$('saveStatus').textContent='Guardando cambios…';clearTimeout(autoSaveTimer);autoSaveTimer=setTimeout(writeDraft,450)}
window.addEventListener('pagehide',writeDraft);
function setMode(v){story=v;$('cardMode').classList.toggle('active',!story);$('storyMode').classList.toggle('active',story)}
function loadSnapshot(p){
  if(!p||typeof p.key!=='string'||!(/^(ready-([0-9]|[12][0-9])|portrait-([0-9]|1[01]))$/).test(p.key)){showMsg('El diseño guardado no es válido');return false}
  restoring=true;
  if(p.key.startsWith('ready-'))readySelect(Number(p.key.slice(6)));
  else{readyId=-1;variant=Number(p.key.slice(9));readyList()}
  setMode(Boolean(p.story));
  for(const k of ['name','phrase','message'])if(typeof p[k]==='string')$(k).value=p[k].slice(0,k==='message'?220:k==='name'?80:110);
  for(const k of ['zoom','panX','panY'])if(p[k]!==undefined){const node=$(k);const v=Number(p[k]);node.value=Number.isFinite(v)?Math.max(Number(node.min),Math.min(Number(node.max),v)):Number(node.defaultValue)}
  restoring=false;updateFavorite();render(true);scheduleDraft();return true;
}
function showMsg(msg){$('saveStatus').textContent=msg}
$('favToggle').onclick=()=>{const k=designKey();if(favSet.has(k))favSet.delete(k);else favSet.add(k);persistMeta();updateFavorite();showMsg(favSet.has(k)?'Añadido a favoritos':'Eliminado de favoritos')};
const phraseSuggestions=['Siempre en nuestros corazones','Tu recuerdo será eterno','Vuela alto','Descansa en paz','Gracias por tanto amor','Una luz que nunca se apaga','Tu memoria florece','Hasta siempre','Que brille para ti la luz perpetua','Te llevamos en el corazón','Hoy mi corazón está de luto','Siempre contigo'];
phraseSuggestions.forEach(t=>{let b=document.createElement('button');b.type='button';b.textContent=t;b.onclick=()=>{$('phrase').value=t;$('phraseList').hidden=true;render();scheduleDraft()};$('phraseList').append(b)});
$('phraseToggle').onclick=()=>{$('phraseList').hidden=!$('phraseList').hidden};
let projectLoadToken=0;
function personalList(keys,source){
  const root=$('personalList');root.hidden=false;root.replaceChildren();
  if(!keys.length){root.textContent='Todavía no tienes diseños aquí.';return}
  keys.forEach((key)=>{
    const row=document.createElement('div');row.className='saved-item';const name=document.createElement('span');name.className='title';
    name.textContent=source==='projects'?(key.name||key.phrase||'Sin título')+' · '+new Date(key.time||Date.now()).toLocaleDateString():key.startsWith('ready-')?readyPresets[Number(key.slice(6))]?.[0]:names[Number(key.slice(9))];
    const open=document.createElement('button');open.textContent='Abrir';
    open.onclick=async()=>{
      const token=++projectLoadToken;++photoSelectionVersion;
      if(source==='projects'){
        editingId=key.id;img=null;photoFile=null;
        if(!loadSnapshot(key))return;
        showMsg('Recuperando tu diseño…');
        try{
          const blob=await getPhoto(key.id);
          if(token!==projectLoadToken)return;
          if(blob){const picture=await imageFromBlob(blob);if(token!==projectLoadToken)return;img=picture;photoFile=blob;}
          showMsg('Proyecto recuperado');
        }catch(e){if(token===projectLoadToken)showMsg('La fotografía no está disponible; puedes añadirla de nuevo')}
        if(token===projectLoadToken)render(true);
      }else{
        editingId=null;
        loadSnapshot({...snapshot(),key,story:key.startsWith('ready-')});personalTouch();
      }
      root.hidden=true;
    };
    row.append(name,open);
    if(source==='projects'){
      const del=document.createElement('button');del.textContent='Eliminar';
      del.onclick=async()=>{
        if(!confirm('¿Eliminar este proyecto local?'))return;
        if(editingId===key.id)editingId=null;
        projects=projects.filter(p=>p.id!==key.id);
        persistMeta();
        try{await removePhoto(key.id)}catch(e){showMsg('No se pudo eliminar la copia de la fotografía')}
        personalList(projects,'projects');
      };row.append(del);
    }
    root.append(row);
  });
}
$('showFavorites').onclick=()=>personalList([...favSet],'favorites');
$('showRecent').onclick=()=>personalList(recentIds,'recent');
$('showProjects').onclick=()=>personalList(projects,'projects');
$('saveProject').onclick=()=>savePersonalProject(false);
$('duplicateProject').onclick=()=>savePersonalProject(true);
async function savePersonalProject(duplicate){
  const p=snapshot();p.id=(!duplicate&&editingId)||(globalThis.crypto?.randomUUID?.()||('memo-'+Date.now()+'-'+Math.random().toString(36).slice(2,9)));
  try{
    if(!img){if(!duplicate&&editingId)await removePhoto(p.id)}
    else if(photoFile){await storePhoto(p.id,photoFile)}
    else{
      const cv=document.createElement('canvas');const ratio=Math.min(1,2300/Math.max(img.naturalWidth,img.naturalHeight));
      cv.width=Math.max(1,Math.round(img.naturalWidth*ratio));cv.height=Math.max(1,Math.round(img.naturalHeight*ratio));
      const ctx=cv.getContext('2d');if(!ctx)throw Error('Canvas no disponible');
      ctx.drawImage(img,0,0,cv.width,cv.height);
      const blob=await new Promise((resolve,reject)=>cv.toBlob(b=>b?resolve(b):reject(Error('Error de imagen')),'image/jpeg',.91));
      await storePhoto(p.id,blob);photoFile=blob;
    }
  }catch(e){showMsg('No se pudo guardar el proyecto con su fotografía. Libera espacio o permite el almacenamiento local.');return}
  projects=[p,...projects.filter(x=>x.id!==p.id)].slice(0,25);
  const stored=persistMeta();editingId=p.id;
  showMsg(!stored?'Guardado solo temporalmente; descarga la imagen PNG para conservarla':duplicate?'Copia independiente guardada en Mis diseños':'Proyecto guardado en Mis diseños');
}
try{const draft=JSON.parse(secureStorage.getItem(STORAGE+'_draft')||'null');if(draft)loadSnapshot(draft)}catch(e){showMsg('No se pudo recuperar el último borrador')};updateFavorite();if(!secureStorage.durable)showMsg('Aviso: almacenamiento web no disponible. Descarga el PNG antes de cerrar.');

render(true);
