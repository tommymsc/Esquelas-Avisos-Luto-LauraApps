from pathlib import Path
from PIL import Image
from playwright.sync_api import sync_playwright
from time import perf_counter
import json,sys
BASE=Path(__file__).resolve().parents[1]
html=(BASE/'demo/index.html').read_text()
photo=(BASE/'tests/test_photo.png')
Image.new('RGB',(800,1200),(190,160,130)).save(photo)
results=[]
def assert_case(name,fn):
 try:
  fn();results.append((name,'PASS'))
 except Exception as e:
  results.append((name,'FAIL '+str(e)[:320]));raise

with sync_playwright() as pw:
 browser=pw.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox','--disable-dev-shm-usage'])
 page=browser.new_page(viewport={'width':410,'height':860},device_scale_factor=1)
 errs=[];page.on('pageerror',lambda e:errs.append(str(e)))
 page.goto('about:blank')
 page.evaluate('''() => {
  const storage=new Map();
  Object.defineProperty(window,'localStorage',{configurable:true,value:{getItem:k=>storage.get(k)??null,setItem:(k,v)=>storage.set(k,String(v)),removeItem:k=>storage.delete(k)}});
  const photos=new Map();
  const db={createObjectStore:()=>{},transaction:()=>{
    const tx={oncomplete:null,onerror:null,error:null,objectStore:()=>({
      put:(value,key)=>{photos.set(key,value);setTimeout(()=>tx.oncomplete?.(),0)},
      get:(key)=>{const req={onsuccess:null,onerror:null,result:null};setTimeout(()=>{req.result=photos.get(key);req.onsuccess?.()},0);return req},
      delete:(key)=>{photos.delete(key);setTimeout(()=>tx.oncomplete?.(),0)}
    })};return tx;
  }};
  Object.defineProperty(window,'indexedDB',{configurable:true,value:{open:()=>{
    const r={result:db,onupgradeneeded:null,onsuccess:null,onerror:null,error:null};
    setTimeout(()=>{r.onupgradeneeded?.();r.onsuccess?.()},0);return r;
  }}});
  window.__tests={storage,photos};
 }''')
 t0=perf_counter();page.set_content(html,wait_until='load',timeout=60000)
 assert_case('Inicializa sin errores JavaScript',lambda:None if not errs else (_ for _ in ()).throw(Exception(errs)))
 assert_case('12 diseños de fotografía',lambda:None if page.locator('.tile').count()==12 else (_ for _ in ()).throw(Exception(page.locator('.tile').count())))
 assert_case('30 estados listos',lambda:None if page.evaluate('readyPresets.length')==30 else (_ for _ in ()).throw(Exception('conteo')))
 # every design and status actual redraw
 def all_cards():
  for v in range(12):page.evaluate('(v)=>{variant=v;readyId=-1;render()}',v)
  for v in range(30):page.evaluate('(v)=>readySelect(v)',v)
  assert page.evaluate('canvas.height')==1920
 assert_case('42 composiciones renderizan',all_cards)
 assert_case('Formato 4:5 y 9:16',lambda:(page.locator('#cardMode').click(),None if page.evaluate('canvas.height')==1350 else (_ for _ in ()).throw(Exception('tarjeta')),page.locator('#storyMode').click(),None if page.evaluate('canvas.height')==1920 else (_ for _ in ()).throw(Exception('estado'))))
 page.locator('#name').fill('Un nombre bastante largo que prueba que la aplicación no se bloquee al escribir y renderizar')
 assert_case('Edición de texto y vista previa',lambda:None if page.evaluate("document.querySelector('#name').value.length>0&&canvas.width===1080") else (_ for _ in ()).throw(Exception('texto')))
 page.locator('#photo').set_input_files(str(photo));page.wait_for_function('img !== null',timeout=10000)
 assert_case('Fotografía y optimización',lambda:None if page.evaluate('Boolean(img)&&photoFile instanceof Blob&&photoFile.size>0') else (_ for _ in ()).throw(Exception('foto')))
 page.locator('#saveProject').click();page.wait_for_function('projects.length===1',timeout=10000)
 assert_case('Guardar proyecto con fotografía',lambda:None if page.evaluate('projects.length===1&&__tests.photos.size===1') else (_ for _ in ()).throw(Exception('persistencia')))
 page.locator('#duplicateProject').click();page.wait_for_function('projects.length===2',timeout=10000)
 assert_case('Duplicar independiente',lambda:None if page.evaluate('projects[0].id!==projects[1].id&&__tests.photos.size===2') else (_ for _ in ()).throw(Exception('duplicado')))
 page.locator('#remove').click();page.locator('#saveProject').click();page.wait_for_function('__tests.photos.size===1',timeout=10000)
 assert_case('Quitar foto sin alterar la copia',lambda:None if page.evaluate('projects.length===2&&__tests.photos.size===1') else (_ for _ in ()).throw(Exception('foto antigua')))
 page.locator('#showProjects').click();page.locator('#personalList button').first.click();page.wait_for_timeout(300)
 assert_case('Restaurar proyecto guardado',lambda:None if page.evaluate('editingId!==null&&document.querySelector("#name").value.length>0') else (_ for _ in ()).throw(Exception('restore')))
 # choose blank state from selection; save no-image project
 page.locator('#remove').click();page.locator('#showProjects').click();page.locator('#personalList button').first.click();page.wait_for_timeout(200)
 assert_case('Sin errores JS tras interacción',lambda:None if not errs else (_ for _ in ()).throw(Exception(str(errs))))
 assert_case('ID de plantilla inválido no rompe el editor',lambda:None if page.evaluate('loadSnapshot({key:"ready-999",name:"Inválido"})') is False else (_ for _ in ()).throw(Exception('id inválido aceptado')))
 # A damaged file must show a friendly error and keep the app responsive.
 page.on('dialog',lambda dialog:dialog.accept())
 page.locator('#photo').set_input_files({'name':'archivo_danado.png','mimeType':'image/png','buffer':b'not a valid png'})
 page.wait_for_timeout(400)
 assert_case('Archivo de imagen dañado se rechaza',lambda:None if 'no se pudo abrir' in page.locator('#saveStatus').inner_text().lower() else (_ for _ in ()).throw(Exception('no mostró el error')))
 page.evaluate('()=>{readySelect(0);render()}')
 page.screenshot(path=str(BASE/'tests/screenshot_editor_v17.png'),full_page=True)
 print('startup_secs',round(perf_counter()-t0,2))
 for a,b in results:print(b,a)
 browser.close()
