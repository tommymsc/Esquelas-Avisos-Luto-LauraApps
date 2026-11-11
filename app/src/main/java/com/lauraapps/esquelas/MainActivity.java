package com.lauraapps.esquelas;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import android.content.ActivityNotFoundException;
import org.json.*;

/** Standalone, offline-first memorial designer. No analytics, account or network permission. */
public class MainActivity extends Activity {
    private static final int BG=Color.rgb(248,247,245), INK=Color.rgb(37,37,41), MUTED=Color.rgb(110,108,109), GOLD=Color.rgb(128,92,50);
    private LinearLayout root, content, toolbar, options;
    private CardView preview;
    private AdsController ads;
    private FrameLayout homeBanner;
    private TextView privacyButton;
    private TextView sectionTitle, subt;
    private final Model model=new Model();
    private String category="Lazos";
    private boolean atHome=true;
    private final String[] categories={"Lazos", "Con foto", "Estados con foto", "Estados listos", "Esquelas", "Novenarios", "Condolencias"};
    private static final int PHOTO_REQUEST=118;
    private final ArrayList<Design> designs=new ArrayList<>();
    private int selected=0;
    private Uri photoUri;
    private Bitmap portrait;
    private final ExecutorService photoWorker=Executors.newSingleThreadExecutor();
    private final ExecutorService exportWorker=Executors.newSingleThreadExecutor();
    private final AtomicBoolean exportBusy=new AtomicBoolean(false);
    private int photoGeneration=0;
    private static final int MAX_IMAGE_EDGE=2300;
    private static final int MAX_PROJECTS=25;
    private static final String PHOTO_DIR="private_portraits";
    private static final String IMAGE_PREFIX="photo_";
    private android.content.SharedPreferences prefs;
    private final ArrayList<JSONObject> savedProjects=new ArrayList<>();
    private final LinkedHashSet<Integer> favorites=new LinkedHashSet<>();
    private final LinkedList<Integer> recent=new LinkedList<>();
    private static final String[][] PHRASES={
        {"Siempre en nuestros corazones","Tu recuerdo será eterno.","Tu amor permanece con nosotros."},
        {"Vuela alto","Gracias por tantos momentos compartidos.","Hasta siempre, con todo nuestro amor."},
        {"Descansa en paz","Que brille para ti la luz perpetua.","Te recordamos con cariño."},
        {"Un amor que no se apaga","Tu recuerdo ilumina nuestros días.","Siempre vivirás en nuestra memoria."}
    };

    static class Model {
        String name="María Elena Rodríguez", date="Siempre en nuestros corazones", message="Tu recuerdo vivirá por siempre en nosotros.", when="Domingo 15 de noviembre · 6:00 p. m.", where="Parroquia de Nuestra Señora";
        boolean photo=false, story=false; float zoom=1f, x=0f, y=0f;
    }
    static class Design {
        final String title, category; final int base, second, accent, ink; final int motif, layout;
        Design(String title,String category,int base,int second,int accent,int ink,int motif,int layout) { this.title=title;this.category=category;this.base=base;this.second=second;this.accent=accent;this.ink=ink;this.motif=motif;this.layout=layout; }
    }
    static int c(String code){return Color.parseColor(code);}
    @Override public void onCreate(Bundle b) {super.onCreate(b); getWindow().setStatusBarColor(c("#161923")); getWindow().setNavigationBarColor(c("#F8F7F5")); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR); prefs=getSharedPreferences("design_drafts",MODE_PRIVATE); seed(); load(); loadCollections(); home(); loadDraftPhotoAsync();
        ads=new AdsController(this,()->runOnUiThread(this::updatePrivacyButton));
        if(homeBanner!=null)ads.attachBanner(homeBanner);
        ads.start();
        if(Build.VERSION.SDK_INT>=33){
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->{
                if(atHome)finish();else home();
            });
        }
    }
    private void seed(){
        String[] names={"Luto clásico","Lirio de luz","Recuerdo dorado","Paz eterna","Alas de esperanza","Cielo sereno","Flor de memoria","Abrazo eterno","Elegancia blanca","Luz entre sombras","Perla y oro","Siempre presente"};
        int[] b={c("#10151B"),c("#F8F5EF"),c("#1C2434"),c("#EDE8E1"),c("#243B45"),c("#FBF8F1"),c("#49323E"),c("#E7DFD4"),c("#FCFBF8"),c("#16161F"),c("#EFE6DA"),c("#263831")};
        int[] a={c("#DBBA85"),c("#A98552"),c("#E5C28C"),c("#AE8A62"),c("#E5CBB3"),c("#687F98"),c("#F4D2CF"),c("#A78062"),c("#9F987E"),c("#D9BE9A"),c("#9C7B5A"),c("#D8C49B")};
        for(int cat=0;cat<categories.length;cat++) {
            if(cat==3){
                for(int i=0;i<STATUS_PRESETS.length;i++){
                    int idx=(i*3+cat)%12;
                    designs.add(new Design(STATUS_PRESETS[i][0],categories[cat],b[idx],blend(b[idx],Color.WHITE,.07f),a[idx],isDark(b[idx])?Color.WHITE:c("#293039"),i,6));
                }
            }else if(cat==1 || cat==2){
                String[] photoNames={"Retrato eterno","Jardín de recuerdos","Elegancia dorada","Luz de amor","Cielo de paz","Flor de vida","Lazo de memoria","Siempre contigo","Retrato de luz","Cielo eterno","Marfil de recuerdos","Jardín nocturno"};
                for(int i=0;i<12;i++){
                    int idx=(i*2+cat*3)%12;
                    designs.add(new Design(photoNames[i],categories[cat],b[idx],blend(b[idx],Color.WHITE,0.07f),a[idx],isDark(b[idx])?Color.WHITE:c("#293039"),i,cat==1?4:5));
                }
            }else{
                int layout=cat==0?0:cat==4?1:cat==5?2:3;
                for(int i=0;i<12;i++){
                    int idx=(i+cat*3)%12;
                    designs.add(new Design(names[i],categories[cat],b[idx],blend(b[idx],Color.WHITE,0.07f),a[idx],isDark(b[idx])?Color.WHITE:c("#293039"),i%4,layout));
                }
            }
        }
    }
    private static int blend(int a,int b,float t){return Color.rgb((int)(Color.red(a)*(1-t)+Color.red(b)*t),(int)(Color.green(a)*(1-t)+Color.green(b)*t),(int)(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    private static boolean isDark(int x){return (Color.red(x)*.299+Color.green(x)*.587+Color.blue(x)*.114)<145;}
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+0.5f);}
    private GradientDrawable shape(int color,float rad){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(rad));return d;}
    private TextView text(String txt,float sp,int color,boolean bold){TextView t=new TextView(this); t.setText(txt);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout vertical(){LinearLayout a=new LinearLayout(this);a.setOrientation(LinearLayout.VERTICAL);return a;}
    private LinearLayout horizontal(){LinearLayout a=new LinearLayout(this);a.setOrientation(LinearLayout.HORIZONTAL);a.setGravity(Gravity.CENTER_VERTICAL);return a;}
    private void space(LinearLayout p,int h){View v=new View(this);p.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
    private TextView click(String title,Runnable r,boolean primary){
        TextView t=text(title,14,primary?Color.WHITE:INK,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12),dp(13),dp(12),dp(13));
        GradientDrawable surface=shape(primary?INK:c("#F0EDE8"),14);
        android.content.res.ColorStateList ripple=android.content.res.ColorStateList.valueOf(primary?0x44FFFFFF:0x22000000);
        t.setBackground(new RippleDrawable(ripple,surface,null));
        t.setMinHeight(dp(50));t.setMaxLines(3);
        t.setContentDescription(title);
        t.setFocusable(true);
        t.setOnClickListener(v->r.run());
        return t;
    }
    private LinearLayout.LayoutParams match(int h){return new LinearLayout.LayoutParams(-1,h<0?h:dp(h));}
    private void updatePrivacyButton(){
        if(privacyButton!=null)privacyButton.setVisibility(
            ads!=null&&ads.privacyOptionsRequired()?View.VISIBLE:View.GONE);
    }
    private void setup(String heading,String caption){
        if(ads!=null)ads.detachBanner();
        homeBanner=null;privacyButton=null;
        atHome="Un recuerdo que abraza".equals(heading);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
        if(Build.VERSION.SDK_INT>=35){
            scroll.setClipToPadding(false);
            scroll.setOnApplyWindowInsetsListener((view,insets)->{
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                scroll.setPadding(0,bars.top,0,bars.bottom);
                return insets;
            });
        }
        root=vertical();scroll.addView(root);setContentView(scroll);
        LinearLayout bar=horizontal();bar.setPadding(dp(20),dp(19),dp(20),dp(10));root.addView(bar);
        LinearLayout brand=vertical();TextView brandName=text("LAURAAPPS  /  MEMORIAS",11,GOLD,true);brand.addView(brandName);TextView top=text("Esquelas & Luto",23,INK,true);brand.addView(top);bar.addView(brand,new LinearLayout.LayoutParams(0,-2,1));TextView heart=text("✦",25,GOLD,true);bar.addView(heart);
        content=vertical();content.setPadding(dp(18),dp(8),dp(18),dp(30));root.addView(content);
        sectionTitle=text(heading,23,INK,true);content.addView(sectionTitle);
        space(content,6);
        subt=text(caption,13,MUTED,false);subt.setLineSpacing(dp(3),1f);content.addView(subt);
        space(content,10);
        View accentLine=new View(this);accentLine.setBackground(shape(GOLD,3));content.addView(accentLine,new LinearLayout.LayoutParams(dp(42),dp(3)));
        space(content,17);
    }
    private static final String[][] STATUS_PRESETS = new String[][]{
        {"Lazo eterno", "Hoy mi corazón está de luto", "Tu recuerdo vivirá siempre en mí."},
        {"Siempre contigo", "Siempre en nuestros corazones", "El amor nunca se despide."},
        {"Vuela alto", "Vuela alto", "Que la paz acompañe tu descanso."},
        {"Hasta siempre", "Hasta siempre", "Gracias por todos los momentos compartidos."},
        {"Una luz eterna", "Tu luz permanece", "Hay recuerdos que jamás se apagan."},
        {"Descansa en paz", "Descansa en paz", "Te recordaremos con amor."},
        {"Lirios de amor", "Con amor eterno", "Tu ausencia se siente, tu amor permanece."},
        {"Rosa blanca", "Una flor para tu recuerdo", "Siempre estarás presente."},
        {"Jardín de memoria", "Floreces en mis recuerdos", "Tu cariño vive en nosotros."},
        {"Amor inmenso", "El amor trasciende", "No hay distancia para los recuerdos."},
        {"Paz de marfil", "Un recuerdo lleno de paz", "Que descanses en serenidad."},
        {"Pétalos de luz", "Tu memoria florece", "Nunca olvidaremos tu sonrisa."},
        {"Vela encendida", "Una vela por ti", "Tu recuerdo ilumina nuestro camino."},
        {"Luz en la noche", "Siempre brillarás", "Ni el tiempo apagará tu luz."},
        {"Luz de esperanza", "La esperanza nos acompaña", "Que encuentres descanso eterno."},
        {"Eterna presencia", "Vives en nuestros pensamientos", "Gracias por tu amor incondicional."},
        {"Recuerdo dorado", "Un amor que permanece", "Cada recuerdo es un abrazo."},
        {"Noche serena", "En paz y en luz", "Te llevamos en el corazón."},
        {"Cielo de paz", "Que el cielo te abrace", "Tu recuerdo nos da fortaleza."},
        {"Paloma blanca", "Vuela en paz", "Siempre te recordaremos."},
        {"Camino de luz", "Hasta volver a encontrarnos", "Guardamos cada instante de amor."},
        {"Oración de paz", "Dale, Señor, el descanso eterno", "Y brille para ti la luz perpetua."},
        {"Fe y recuerdo", "En manos de Dios", "Conservamos tu memoria con amor."},
        {"Cielo luminoso", "Descanso y esperanza", "Que la paz te acompañe."},
        {"Te extraño", "Te extraño cada día", "Tu ausencia deja un silencio lleno de recuerdos."},
        {"Gracias por tanto", "Gracias por tanto amor", "Cada enseñanza tuya sigue conmigo."},
        {"Por siempre mamá", "Mamá, siempre contigo", "Tu amor seguirá guiándome."},
        {"Por siempre papá", "Papá, nunca te olvidaremos", "Tu ejemplo vive en cada paso."},
        {"Un aniversario más", "Hoy te recordamos", "El tiempo pasa, el amor permanece."},
        {"Amistad eterna", "Amigos para siempre", "Tu amistad será un recuerdo precioso."}
    };
    private void useStatusPreset(int index){
        String[] p=STATUS_PRESETS[index];model.name="";model.date=p[1];model.message=p[2];model.story=true;saveDraft();
    }
    private void loadCollections(){
        try{JSONArray a=new JSONArray(prefs.getString("favorite_ids","[]"));for(int i=0;i<a.length();i++){int id=a.optInt(i,-1);if(id>=0&&id<designs.size())favorites.add(id);}}catch(Exception ignored){}
        try{JSONArray a=new JSONArray(prefs.getString("recent_ids","[]"));for(int i=0;i<a.length();i++){int id=a.optInt(i,-1);if(id>=0&&id<designs.size()&&!recent.contains(id))recent.add(id);}}catch(Exception ignored){}
        try{JSONArray a=new JSONArray(prefs.getString("saved_projects","[]"));HashSet<String> used=new HashSet<>();
            for(int i=0;i<a.length()&&savedProjects.size()<MAX_PROJECTS;i++){
                JSONObject o=a.optJSONObject(i);
                if(o==null||o.optInt("design",-1)<0||o.optInt("design",-1)>=designs.size())continue;
                String uid=o.optString("id","");
                if(uid.isEmpty()||used.contains(uid)){
                    uid=UUID.randomUUID().toString();
                    try{o.put("id",uid);}catch(JSONException ignored){}
                }
                used.add(uid);savedProjects.add(o);
            }}catch(Exception ignored){}
        selected=ProjectRules.clampDesign(prefs.getInt("selected_design",selected),designs.size());
        persistCollections();
        model.story=prefs.getBoolean("story_format",model.story);
    }
    private void persistCollections(){
        JSONArray f=new JSONArray(),r=new JSONArray(),p=new JSONArray();for(Integer id:favorites)f.put(id);for(Integer id:recent)r.put(id);for(JSONObject o:savedProjects)p.put(o);
        prefs.edit().putString("favorite_ids",f.toString()).putString("recent_ids",r.toString()).putString("saved_projects",p.toString()).apply();
    }
    private void rememberDesign(int id){recent.remove((Integer)id);recent.addFirst(id);while(recent.size()>12)recent.removeLast();persistCollections();}
    private void openDesign(int id){prefs.edit().remove("editing_project_id").remove("editing_project").apply();selected=id;Design d=designs.get(selected);category=d.category;model.story=d.layout==5||d.layout==6;if(d.layout==6)useStatusPreset(d.motif);rememberDesign(id);saveDraft();editor();}
    private void toggleFavorite(){if(favorites.contains(selected))favorites.remove(selected);else favorites.add(selected);persistCollections();editor();toast(favorites.contains(selected)?"Guardado en favoritos":"Quitado de favoritos");}
    private JSONObject snapshot(){JSONObject o=new JSONObject();try{o.put("design",selected);o.put("name",model.name);o.put("date",model.date);o.put("message",model.message);o.put("when",model.when);o.put("where",model.where);o.put("zoom",model.zoom);o.put("x",model.x);o.put("y",model.y);o.put("story",model.story);o.put("photo",photoUri==null?"":photoUri.toString());o.put("savedAt",System.currentTimeMillis());}catch(Exception ignored){}return o;}
    private void saveProject(boolean duplicate){
        JSONObject project=snapshot();
        String oldId=prefs.getString("editing_project_id","");
        String id=(!duplicate&&!oldId.isEmpty())?oldId:UUID.randomUUID().toString();
        try{project.put("id",id);}catch(JSONException ignored){}
        int existing=-1;
        for(int i=0;i<savedProjects.size();i++)if(id.equals(savedProjects.get(i).optString("id"))){existing=i;break;}
        if(existing>=0)savedProjects.remove(existing);
        savedProjects.add(0,project);
        while(savedProjects.size()>MAX_PROJECTS)savedProjects.remove(savedProjects.size()-1);
        prefs.edit().putString("editing_project_id",id).remove("editing_project").apply();
        persistCollections();queuePhotoCleanup();toast(duplicate?"Copia guardada en Mis diseños":"Proyecto guardado en Mis diseños");
    }
    private void restoreProject(int index){
        if(index<0||index>=savedProjects.size())return;
        JSONObject o=savedProjects.get(index);
        selected=ProjectRules.clampDesign(o.optInt("design",0),designs.size());
        category=designs.get(selected).category;
        model.name=ProjectRules.cleanText(o.optString("name",""),80);
        model.date=ProjectRules.cleanText(o.optString("date",""),110);
        model.message=ProjectRules.cleanText(o.optString("message",""),220);
        model.when=ProjectRules.cleanText(o.optString("when",""),110);
        model.where=ProjectRules.cleanText(o.optString("where",""),110);
        model.zoom=ProjectRules.clampFloat(o.optDouble("zoom",1),1f,2.1f);
        model.x=ProjectRules.clampFloat(o.optDouble("x",0),-100f,100f);
        model.y=ProjectRules.clampFloat(o.optDouble("y",0),-100f,100f);
        model.story=o.optBoolean("story",false);
        String u=o.optString("photo","");
        photoGeneration++;
        portrait=null;photoUri=parseSafePhotoUri(u);
        String id=o.optString("id","");
        if(id.isEmpty()){
            id=UUID.randomUUID().toString();
            try{o.put("id",id);}catch(JSONException ignored){}
            persistCollections();
        }
        prefs.edit().putString("editing_project_id",id).remove("editing_project").apply();
        rememberDesign(selected);saveDraft();editor();
        if(photoUri!=null)loadPhotoAsync(photoUri,photoGeneration,true);
        else if(!u.isEmpty())toast("La fotografía de este proyecto ya no está disponible.");
    }
    private void showSaved(){setup("Mis diseños","Recupera, duplica o elimina tus homenajes guardados. Todo queda en tu dispositivo.");if(savedProjects.isEmpty()){content.addView(text("Todavía no tienes diseños guardados. Abre una plantilla y pulsa Guardar proyecto.",14,MUTED,false));return;}for(int i=0;i<savedProjects.size();i++){final int ix=i;JSONObject o=savedProjects.get(i);int id=o.optInt("design");LinearLayout line=horizontal();line.setPadding(dp(8),dp(8),dp(8),dp(8));line.setBackground(shape(Color.WHITE,12));LinearLayout.LayoutParams lp=match(-2);lp.setMargins(0,0,0,dp(8));content.addView(line,lp);TextView name=text(o.optString("name",designs.get(id).title),14,INK,true);line.addView(name,new LinearLayout.LayoutParams(0,-2,1));line.addView(click("Abrir",()->restoreProject(ix),false));line.addView(click("✕",()->new AlertDialog.Builder(this).setTitle("Eliminar diseño guardado").setMessage("¿Quieres eliminar esta copia? No se eliminará ninguna foto de tu galería.").setPositiveButton("Eliminar",(d,w)->{savedProjects.remove(ix);prefs.edit().remove("editing_project_id").remove("editing_project").apply();persistCollections();queuePhotoCleanup();showSaved();}).setNegativeButton("Cancelar",null).show(),false));}}
    private void showFavorites(){setup("Mis favoritos","Tus lazos y composiciones favoritas a un toque.");if(favorites.isEmpty()){content.addView(text("Toca ♡ en un diseño para encontrarlo aquí.",14,MUTED,false));return;}for(Integer ix:favorites){content.addView(click("♥  "+designs.get(ix).title+"  →",()->openDesign(ix),false),match(-2));space(content,8);}}
    private void showRecent(){setup("Recientes","Vuelve a los diseños que has utilizado.");if(recent.isEmpty()){content.addView(text("Aquí aparecerán tus últimos diseños utilizados.",14,MUTED,false));return;}for(Integer ix:recent){content.addView(click("↺  "+designs.get(ix).title+"  →",()->openDesign(ix),false),match(-2));space(content,8);}}
    private void phrasePicker(){String[] list={"Siempre en nuestros corazones","Tu recuerdo será eterno","Vuela alto","Descansa en paz","Tu amor permanece","Hoy mi corazón está de luto","Que brille para ti la luz perpetua","Gracias por tantos momentos","Te llevamos siempre en el corazón","Una luz que nunca se apaga","Hasta siempre","Tu memoria florece"};new AlertDialog.Builder(this).setTitle("Elige una frase").setItems(list,(d,i)->{model.date=list[i];saveDraft();editor();}).setNegativeButton("Cancelar",null).show();}
    private void home(){
        setup("Un recuerdo que abraza","Diseños hermosos, hechos con cariño. Personaliza y comparte en minutos.");
        LinearLayout quick=horizontal();content.addView(quick);
        boolean narrow=getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density<360;
        if(narrow)quick.setOrientation(LinearLayout.VERTICAL);
        for(String title:new String[]{"♡ Favoritos","↺ Recientes","▦ Mis diseños"}){
            Runnable action=title.startsWith("♡")?this::showFavorites:title.startsWith("↺")?this::showRecent:this::showSaved;
            TextView b=click(title,action,false);
            LinearLayout.LayoutParams q=narrow?new LinearLayout.LayoutParams(-1,-2):new LinearLayout.LayoutParams(0,-2,1);
            q.setMargins(0,0,narrow?0:dp(5),dp(10));
            quick.addView(b,q);
        }
        space(content,8);
        HorizontalScrollView tabsScroll=new HorizontalScrollView(this);tabsScroll.setHorizontalScrollBarEnabled(false);content.addView(tabsScroll);
        LinearLayout tabs=horizontal();tabs.setPadding(0,0,0,dp(9));tabsScroll.addView(tabs);
        for(String cat:categories){TextView chip=text(cat,12,cat.equals(category)?Color.WHITE:INK,true);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(14),dp(12),dp(14),dp(12));chip.setBackground(shape(cat.equals(category)?INK:c("#EEEAE5"),13));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(42));lp.setMargins(dp(2),0,dp(5),0);tabs.addView(chip,lp);chip.setOnClickListener(v->{category=cat;home();});}
        LinearLayout hero=vertical();hero.setPadding(dp(18),dp(14),dp(18),dp(14));hero.setBackground(shape(c("#23252C"),20));content.addView(hero,match(-2));TextView h=text("✦  Tu homenaje, a tu manera",18,Color.WHITE,true);hero.addView(h);space(hero,7);TextView s=text("Lazos que quieres compartir, retratos que abrazan y estados preparados para WhatsApp.",13,c("#E3DCD1"),false);hero.addView(s);space(content,18);
        // Ads appear only on the library screen, never inside the editor, preview or share flow.
        homeBanner=new FrameLayout(this);
        homeBanner.setVisibility(View.GONE);
        LinearLayout.LayoutParams adMargin=match(-2);
        adMargin.setMargins(0,dp(12),0,dp(12));
        content.addView(homeBanner,adMargin);
        if(ads!=null)ads.attachBanner(homeBanner);
        TextView heading=text("Colección · "+category,19,INK,true);content.addView(heading);space(content,4);content.addView(text("Elige un diseño y hazlo tuyo",12,MUTED,false));space(content,12);
        for(int i=0;i<designs.size();i++){
            Design d=designs.get(i);if(!d.category.equals(category))continue; final int ix=i;
            LinearLayout item=horizontal();item.setPadding(dp(10),dp(10),dp(12),dp(10));item.setBackground(shape(Color.WHITE,18));LinearLayout.LayoutParams ip=match(-2);ip.setMargins(0,0,0,dp(10));content.addView(item,ip);
            DesignView th=new DesignView(this,d,false);item.addView(th,new LinearLayout.LayoutParams(dp(87),dp(113)));
            LinearLayout info=vertical();info.setPadding(dp(13),0,dp(3),0);item.addView(info,new LinearLayout.LayoutParams(0,-2,1));space(info,8);info.addView(text(d.title,16,INK,true));space(info,6);info.addView(text(d.layout==0?"Lazo listo para estado":d.layout==1?"Aviso personalizado":d.layout==2?"Invitación a ceremonia":d.layout==4?"Retrato personalizado":d.layout==5?"Estado con fotografía":d.layout==6?"Listo para compartir · Sin foto":"Un mensaje de cariño",12,MUTED,false));space(info,11);TextView edit=text("Personalizar  →",13,GOLD,true);info.addView(edit);
            item.setContentDescription(d.title+". Personalizar diseño");
            item.setFocusable(true);item.setClickable(true);
            item.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x18000000),shape(Color.WHITE,18),null));
            item.setOnClickListener(v->openDesign(ix));
        }
        space(content,7);content.addView(text("♥  Diseña sin registro · Fotografías privadas · Diseños hechos con respeto",11,MUTED,false));
        privacyButton=click("Opciones de privacidad y anuncios",()->{if(ads!=null)ads.showPrivacyOptions();},false);
        content.addView(privacyButton,match(-2));updatePrivacyButton();
        if(ads!=null)ads.onHomeAfterExport();
    }
    private void editor(){
        Design d=designs.get(selected);setup("Personaliza tu recuerdo",""+d.title+" · Toca las opciones y observa los cambios al instante.");
        content.addView(text("PASO 1 · Elige el estilo    PASO 2 · Personaliza    PASO 3 · Comparte",11,GOLD,true));space(content,10);
        LinearLayout actions=horizontal();content.addView(actions);TextView back=click("← Diseños",this::home,false);actions.addView(back,new LinearLayout.LayoutParams(0,-2,1));TextView size=click(model.story?"Formato: Estado":"Formato: Tarjeta",()->{model.story=!model.story;saveDraft();editor();},false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1.55f);lp.setMargins(dp(8),0,0,0);actions.addView(size,lp);space(content,12);
        FrameLayout holder=new FrameLayout(this);holder.setBackground(shape(c("#ECE7E0"),18));holder.setPadding(dp(10),dp(10),dp(10),dp(10));content.addView(holder,match(model.story?450:350));
        preview=new CardView(this,d,true);FrameLayout.LayoutParams pr=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER);holder.addView(preview,pr);
        space(content,15);LinearLayout row=horizontal();content.addView(row);row.addView(click("✦  Cambiar diseño",()->choose(),false),new LinearLayout.LayoutParams(0,-2,1));TextView pic=click("▣  "+(portrait==null?"Añadir foto":"Cambiar foto"),this::pickPhoto,false);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,-2,1);pp.setMargins(dp(8),0,0,0);if(d.layout==4||d.layout==5)row.addView(pic,pp);
        if(d.layout==4||d.layout==5){
            if(portrait==null){
                space(content,9);
                TextView photoHelp=text("▣  Añade una fotografía para completar este recuerdo. No se exportará sin imagen.",12,c("#80633D"),true);
                photoHelp.setPadding(dp(12),dp(10),dp(12),dp(10));photoHelp.setBackground(shape(c("#F6ECDD"),12));
                content.addView(photoHelp,match(-2));
            }
            space(content,14);content.addView(text("ENCUADRE DE TU FOTO",12,GOLD,true));space(content,8);
            content.addView(text("Ajusta el retrato si deseas acercarlo o moverlo. El recorte se aplicará a la imagen final.",12,MUTED,false));space(content,9);
            sliderControl("Acercar",100,210,(int)(model.zoom*100),v->{model.zoom=v/100f;preview.invalidate();saveDraft();});
            sliderControl("Mover horizontalmente",0,200,(int)(model.x+100),v->{model.x=v-100;preview.invalidate();saveDraft();});
            sliderControl("Mover verticalmente",0,200,(int)(model.y+100),v->{model.y=v-100;preview.invalidate();saveDraft();});
            content.addView(click("Restablecer encuadre",()->{model.zoom=1;model.x=0;model.y=0;editor();saveDraft();},false),match(-2));
            space(content,8);
            if(portrait!=null)content.addView(click("Quitar fotografía",()->{photoGeneration++;portrait=null;photoUri=null;saveDraft();editor();queuePhotoCleanup();},false),match(-2));
        }
        space(content,10);LinearLayout helpers=horizontal();content.addView(helpers);
        TextView favButton=click(favorites.contains(selected)?"♥ Favorito":"♡ Favorito",this::toggleFavorite,false);helpers.addView(favButton,new LinearLayout.LayoutParams(0,-2,1));
        TextView phraseButton=click("✦ Frases sugeridas",this::phrasePicker,false);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,-2,1.4f);hp.setMargins(dp(8),0,0,0);helpers.addView(phraseButton,hp);
        space(content,17);content.addView(text("TU MENSAJE",12,GOLD,true));space(content,7);
        field("Nombre o dedicatoria",model.name,txt->{model.name=txt;preview.invalidate();saveDraft();});
        field("Frase principal",model.date,txt->{model.date=txt;preview.invalidate();saveDraft();});
        field("Mensaje personal",model.message,txt->{model.message=txt;preview.invalidate();saveDraft();});
        if(d.layout==1||d.layout==2){field("Fecha y hora de la ceremonia",model.when,txt->{model.when=txt;preview.invalidate();saveDraft();});field("Lugar",model.where,txt->{model.where=txt;preview.invalidate();saveDraft();});}
        LinearLayout projectButtons=horizontal();content.addView(projectButtons);projectButtons.addView(click("▣ Guardar proyecto",()->saveProject(false),false),new LinearLayout.LayoutParams(0,-2,1));TextView dup=click("⧉ Duplicar",()->saveProject(true),false);LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(0,-2,1);dl.setMargins(dp(8),0,0,0);projectButtons.addView(dup,dl);space(content,7);content.addView(text("Tus cambios se guardan automáticamente. Las copias independientes se conservan en Mis diseños.",11,MUTED,false));
        space(content,12);content.addView(text("COMPARTE UN RECUERDO",12,GOLD,true));space(content,8);
        LinearLayout bottom=horizontal();content.addView(bottom);bottom.addView(click("↓  Guardar imagen",()->export(false),false),new LinearLayout.LayoutParams(0,-2,1));TextView share=click("↗  Compartir",()->export(true),true);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1);sp.setMargins(dp(8),0,0,0);bottom.addView(share,sp);space(content,12);
        content.addView(text("PNG 1080 px · Sin marca de agua · Procesamiento privado en tu teléfono",11,MUTED,false));
    }
    interface Change {void value(String s);}
    interface ProgressChange {void value(int n);}
    private void sliderControl(String label,int min,int max,int current,ProgressChange change){
        content.addView(text(label,12,MUTED,true));SeekBar bar=new SeekBar(this);bar.setMax(max-min);bar.setProgress(Math.max(0,Math.min(max-min,current-min)));content.addView(bar,match(-2));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int v,boolean from){if(from)change.value(v+min);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
    }
    private void field(String label,String value,Change change){
        TextView l=text(label,12,MUTED,false);content.addView(l);space(content,5);EditText e=new EditText(this);e.setSingleLine(!label.equals("Mensaje personal"));e.setTextSize(15);e.setTextColor(INK);e.setPadding(dp(14),dp(10),dp(14),dp(10));e.setBackground(shape(Color.WHITE,12));e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(label.equals("Mensaje personal")?220:label.equals("Nombre o dedicatoria")?80:110)});e.setText(value);content.addView(e,match(-2));space(content,11);e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int start,int before,int count){change.value(s.toString());}public void afterTextChanged(Editable ed){}});
    }
    private void choose(){final ArrayList<String> names=new ArrayList<>();final ArrayList<Integer> ids=new ArrayList<>();for(int i=0;i<designs.size();i++)if(designs.get(i).category.equals(category)){ids.add(i);names.add(designs.get(i).title);}new AlertDialog.Builder(this).setTitle("Elige otro diseño").setItems(names.toArray(new String[0]),(di,which)->{openDesign(ids.get(which));}).setNegativeButton("Cancelar",null).show();}
    private void pickPhoto(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try{startActivityForResult(i,PHOTO_REQUEST);}
        catch(ActivityNotFoundException e){toast("No se encontró un selector de fotografías.");}
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req!=PHOTO_REQUEST||result!=RESULT_OK||data==null)return;
        Uri incoming=data.getData();
        if(incoming==null||!"content".equals(incoming.getScheme())){
            toast("El archivo seleccionado no es compatible.");return;
        }
        // ACTION_OPEN_DOCUMENT grants temporary read access; photo is copied privately
        // immediately, so no new long-lived document permission is retained.
        final int token=++photoGeneration;
        toast("Preparando fotografía…");
        photoWorker.execute(()->{
            Bitmap loaded=null;Uri internal=null;String failure=null;
            try{
                loaded=readPhoto(incoming);
                if(loaded==null)throw new IOException("Imagen vacía");
                internal=storePrivatePhoto(loaded);
            }catch(Exception | OutOfMemoryError e){failure="No se pudo abrir la fotografía. Prueba con un JPG o PNG diferente.";}
            final Bitmap preparedBitmap=loaded;final Uri uri=internal;final String error=failure;
            runOnUiThread(()->{
                if(isDestroyed()||token!=photoGeneration)return;
                if(error!=null){toast(error);return;}
                portrait=preparedBitmap;photoUri=uri;
                model.zoom=1f;model.x=0;model.y=0;
                saveDraft();editor();queuePhotoCleanup();
            });
        });
    }
    /** All photos used by the editor are bounded in resolution. Runs off the main thread. */
    private Bitmap readPhoto(Uri uri) throws IOException {
        if(uri==null)throw new IOException("Sin fotografía");
        ImageDecoder.Source source;
        if("file".equals(uri.getScheme())){
            File f=safeInternalPhoto(uri);
            if(f==null||!f.isFile())throw new IOException("Fotografía no disponible");
            source=ImageDecoder.createSource(f);
        }else if("content".equals(uri.getScheme())){
            source=ImageDecoder.createSource(getContentResolver(),uri);
        }else throw new IOException("Origen de fotografía no permitido");
        return ImageDecoder.decodeBitmap(source,(decoder,info,src)->{
            int edge=Math.max(info.getSize().getWidth(),info.getSize().getHeight());
            if(edge<=0)throw new IllegalArgumentException("Dimensiones inválidas");
            int sample=Math.max(1,(int)Math.ceil(edge/(double)MAX_IMAGE_EDGE));
            decoder.setTargetSampleSize(sample);
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
    }
    private File safeInternalPhoto(Uri u){
        if(u==null||!"file".equals(u.getScheme()))return null;
        try{
            File f=new File(u.getPath()).getCanonicalFile();
            File parent=new File(getFilesDir(),PHOTO_DIR).getCanonicalFile();
            if(!f.getParentFile().equals(parent)||!ProjectRules.validPhotoName(f.getName()))return null;
            return f;
        }catch(Exception e){return null;}
    }
    private Uri parseSafePhotoUri(String value){
        if(value==null||value.isEmpty())return null;
        try{
            Uri u=Uri.parse(value);
            if("content".equals(u.getScheme()))return u; // migrated below from v1.6
            return safeInternalPhoto(u)==null?null:u;
        }catch(Exception e){return null;}
    }
    private Uri storePrivatePhoto(Bitmap bm) throws IOException {
        File dir=new File(getFilesDir(),PHOTO_DIR);
        if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("No se pudo preparar el almacenamiento privado");
        String name=IMAGE_PREFIX+UUID.randomUUID()+".jpg";
        File dest=new File(dir,name);File partial=new File(dir,name+".tmp");
        try{
            try(FileOutputStream out=new FileOutputStream(partial)){
                if(!bm.compress(Bitmap.CompressFormat.JPEG,91,out))throw new IOException("No se pudo copiar la fotografía");
                out.flush();out.getFD().sync();
            }
            if(!partial.renameTo(dest))throw new IOException("No se pudo completar la fotografía");
            return Uri.fromFile(dest);
        }finally{if(partial.exists())partial.delete();}
    }
    private void loadDraftPhotoAsync(){
        if(photoUri!=null)loadPhotoAsync(photoUri,++photoGeneration,true);
    }
    private void loadPhotoAsync(Uri requested,int token,boolean migrate){
        photoWorker.execute(()->{
            Bitmap bm=null;Uri stored=requested;String failure=null;
            try{
                bm=readPhoto(requested);
                if(migrate&&"content".equals(requested.getScheme()))stored=storePrivatePhoto(bm);
            }catch(Exception | OutOfMemoryError e){failure="No se pudo recuperar la fotografía. Puedes seleccionarla nuevamente.";}
            final Bitmap result=bm;final Uri finalUri=stored;final String warning=failure;
            runOnUiThread(()->{
                if(isDestroyed()||token!=photoGeneration)return;
                if(warning!=null){portrait=null;photoUri=null;saveDraft();toast(warning);return;}
                portrait=result;photoUri=finalUri;saveDraft();
                if(preview!=null)preview.invalidate();
                // An old content:// URI is updated in the currently opened project.
                String current=prefs.getString("editing_project_id","");
                if(!current.isEmpty())for(JSONObject o:savedProjects)if(current.equals(o.optString("id"))){
                    try{o.put("photo",finalUri.toString());persistCollections();}catch(JSONException ignored){}
                    break;
                }
                queuePhotoCleanup();
            });
        });
    }
    private void queuePhotoCleanup(){
        HashSet<String> keep=new HashSet<>();
        Uri active=photoUri;
        File activeFile=safeInternalPhoto(active);
        if(activeFile!=null)keep.add(activeFile.getName());
        for(JSONObject o:savedProjects){
            File f=safeInternalPhoto(parseSafePhotoUri(o.optString("photo","")));
            if(f!=null)keep.add(f.getName());
        }
        photoWorker.execute(()->{
            File dir=new File(getFilesDir(),PHOTO_DIR);
            File[] all=dir.listFiles();if(all==null)return;
            for(File f:all)if(f.isFile()&&ProjectRules.validPhotoName(f.getName())&&!keep.contains(f.getName()))f.delete();
        });
    }
    private void export(boolean share){
        Design current=designs.get(selected);
        if((current.layout==4||current.layout==5)&&portrait==null){
            new AlertDialog.Builder(this)
                .setTitle("Falta la fotografía")
                .setMessage("Añade una fotografía para completar este homenaje antes de guardarlo o compartirlo.")
                .setPositiveButton("Elegir fotografía",(dialog,which)->pickPhoto())
                .setNegativeButton("Cancelar",null).show();
            return;
        }
        if(!exportBusy.compareAndSet(false,true)){toast("Ya estamos preparando tu imagen.");return;}
        Bitmap buffer=null;
        try{
            int h=model.story?1920:1350;
            buffer=Bitmap.createBitmap(1080,h,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(buffer);render(canvas,1080,h,designs.get(selected));
        }catch(Exception | OutOfMemoryError e){
            if(buffer!=null&&!buffer.isRecycled())buffer.recycle();
            exportBusy.set(false);toast("No hay memoria suficiente para crear la imagen. Cierra otras apps e inténtalo otra vez.");return;
        }
        final Bitmap out=buffer;
        toast("Preparando imagen de alta calidad…");
        exportWorker.execute(()->{
            Uri uri=null;String failure=null;
            try{
                ContentValues values=new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,"Esquelas_LauraApps_"+System.currentTimeMillis()+".png");
                values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Esquelas y Avisos de Luto");
                values.put(MediaStore.Images.Media.IS_PENDING,1);
                uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                if(uri==null)throw new IOException("La galería no permitió crear el archivo");
                try(OutputStream os=getContentResolver().openOutputStream(uri,"w")){
                    if(os==null||!out.compress(Bitmap.CompressFormat.PNG,100,os))throw new IOException("La imagen no pudo escribirse");
                    os.flush();
                }
                ContentValues done=new ContentValues();done.put(MediaStore.Images.Media.IS_PENDING,0);
                if(getContentResolver().update(uri,done,null,null)<1)throw new IOException("No se pudo finalizar la imagen");
            }catch(Exception | OutOfMemoryError e){
                failure="No fue posible guardar la imagen. Comprueba el espacio disponible.";
                if(uri!=null)try{getContentResolver().delete(uri,null,null);}catch(Exception ignored){}
            }finally{out.recycle();}
            Uri completed=uri;String error=failure;
            exportBusy.set(false);
            runOnUiThread(()->{
                if(isDestroyed())return;
                if(error!=null){new AlertDialog.Builder(this).setTitle("No se pudo guardar").setMessage(error).setPositiveButton("Entendido",null).show();return;}
                if(share){
                    try{
                        Intent send=new Intent(Intent.ACTION_SEND);
                        send.setType("image/png");
                        send.putExtra(Intent.EXTRA_STREAM,completed);
                        send.setClipData(ClipData.newUri(getContentResolver(),"Recuerdo",completed));
                        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent chooser=Intent.createChooser(send,"Compartir recuerdo");
                        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(chooser);
                    }catch(Exception e){toast("Imagen guardada en la galería. No fue posible abrir el menú para compartir.");}
                }else {
                    if(ads!=null)ads.completedImageDownload();
                    toast("Imagen guardada en tu galería ♥");
                }
            });
        });
    }
    private void toast(String t){Toast.makeText(this,t,Toast.LENGTH_LONG).show();}
    private void saveDraft(){
        android.content.SharedPreferences.Editor edit=prefs.edit()
            .putInt("selected_design",selected).putBoolean("story_format",model.story)
            .putString("name",model.name).putString("date",model.date)
            .putString("message",model.message).putString("when",model.when)
            .putString("where",model.where).putFloat("zoom",model.zoom)
            .putFloat("offset_x",model.x).putFloat("offset_y",model.y);
        if(photoUri==null)edit.remove("photo");else edit.putString("photo",photoUri.toString());
        edit.apply();
    }
    private void load(){
        model.name=ProjectRules.cleanText(prefs.getString("name",model.name),80);
        model.date=ProjectRules.cleanText(prefs.getString("date",model.date),110);
        model.message=ProjectRules.cleanText(prefs.getString("message",model.message),220);
        model.when=ProjectRules.cleanText(prefs.getString("when",model.when),110);
        model.where=ProjectRules.cleanText(prefs.getString("where",model.where),110);
        model.story=prefs.getBoolean("story_format",false);
        model.zoom=ProjectRules.clampFloat(prefs.getFloat("zoom",1f),1f,2.1f);
        model.x=ProjectRules.clampFloat(prefs.getFloat("offset_x",0f),-100f,100f);
        model.y=ProjectRules.clampFloat(prefs.getFloat("offset_y",0f),-100f,100f);
        photoUri=parseSafePhotoUri(prefs.getString("photo",null));
        if(photoUri==null)prefs.edit().remove("photo").apply();
    }
    @Override protected void onDestroy(){
        photoGeneration++;
        photoWorker.shutdown();exportWorker.shutdown();
        if(ads!=null)ads.destroy();
        super.onDestroy();
    }
    @Override public void onBackPressed(){if(atHome)super.onBackPressed();else home();}
    private class CardView extends View {
        private final Design d;
        CardView(Context ctx,Design design,boolean f){super(ctx);d=design;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas actual){
            super.onDraw(actual);
            if(getWidth()<=0||getHeight()<=0)return;
            int h=model.story?1920:1350;int w=1080;
            float scale=Math.min(getWidth()/(float)w,getHeight()/(float)h);
            actual.save();
            try{
                actual.translate((getWidth()-w*scale)/2,(getHeight()-h*scale)/2);
                actual.scale(scale,scale);render(actual,w,h,d);
            }finally{actual.restore();}
        }
    }
    private class DesignView extends View {
        Design d;boolean big;DesignView(Context ctx,Design design,boolean b){super(ctx);d=design;big=b;}
        @Override protected void onDraw(Canvas actual){super.onDraw(actual);float s=Math.min(getWidth()/1080f,getHeight()/1350f);actual.save();actual.translate((getWidth()-1080*s)/2,(getHeight()-1350*s)/2);actual.scale(s,s);render(actual,1080,1350,d);actual.restore();}
    }
    private void render(Canvas ca,int w,int h,Design d){
        Paint p=new Paint(3);p.setShader(new LinearGradient(0,0,w,h,d.second,d.base,Shader.TileMode.CLAMP));ca.drawRect(0,0,w,h,p);p.setShader(null);
        // Soft corner illumination
        p.setShader(new RadialGradient(w*.8f,h*.2f,w*.8f,new int[]{withAlpha(d.accent,85),withAlpha(d.accent,0)},null,Shader.TileMode.CLAMP));ca.drawRect(0,0,w,h,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(withAlpha(d.accent,190));ca.drawRoundRect(40,40,w-40,h-40,28,28,p);p.setStrokeWidth(1.2f);ca.drawRoundRect(57,57,w-57,h-57,20,20,p);p.setStyle(Paint.Style.FILL);
        int textColor=d.ink;int yShift=h==1920?130:0;
        if(d.layout==6){
            renderReadyStatus(ca,w,h,d);
        } else if(d.layout==0){
            drawFlower(ca,835,280+yShift,115,d.accent,Color.WHITE,d.motif);drawRibbon(ca,540,465+yShift,250,d.accent);
            small(ca,"EN MEMORIA DE",540,740+yShift,30,d.accent,true);
            headline(ca,model.name,540,840+yShift,54,textColor,875);
            line(ca,440,900+yShift,640,900+yShift,d.accent,3);
            headline(ca,model.date,540,995+yShift,35,textColor,850);
            headline(ca,model.message,540,1110+yShift,31,textColor,850);
        } else if(d.layout==1){
            drawFlower(ca,825,195+yShift,120,d.accent,Color.WHITE,d.motif);
            small(ca,"CON PROFUNDO AMOR Y RESPETO",540,200+yShift,28,d.accent,true);
            headline(ca,"AVISO DE FALLECIMIENTO",540,315+yShift,47,textColor,890);
            if(portrait!=null)drawPortrait(ca,540,490+yShift,140,d.accent);else drawRibbon(ca,540,500+yShift,132,d.accent);
            headline(ca,model.name,540,735+yShift,57,textColor,880);
            headline(ca,model.date,540,815+yShift,34,d.accent,880);
            headline(ca,model.message,540,925+yShift,32,textColor,870);
            small(ca,model.when,540,1090+yShift,27,textColor,false);
            small(ca,model.where,540,1140+yShift,27,textColor,false);
        } else if(d.layout==2){
            drawFlower(ca,835,250+yShift,110,d.accent,Color.WHITE,d.motif);
            drawRibbon(ca,540,330+yShift,130,d.accent);
            small(ca,"CON AMOR RECORDAMOS",540,510+yShift,26,d.accent,true);
            headline(ca,"NOVENARIO Y ORACIÓN",540,620+yShift,51,textColor,900);
            headline(ca,model.name,540,770+yShift,57,textColor,870);
            headline(ca,model.date,540,845+yShift,34,d.accent,890);
            headline(ca,model.when,540,995+yShift,37,textColor,880);
            headline(ca,model.where,540,1065+yShift,32,textColor,880);
            headline(ca,model.message,540,1170+yShift,28,textColor,870);
        } else if(d.layout==4 || d.layout==5){
            renderPortraitCard(ca,w,h,d);
        } else {
            drawFlower(ca,540,390+yShift,210,d.accent,Color.WHITE,d.motif);
            small(ca,"EN LOS RECUERDOS, SIEMPRE",540,625+yShift,28,d.accent,true);
            headline(ca,model.date,540,760+yShift,51,textColor,890);
            headline(ca,model.name,540,880+yShift,47,textColor,890);
            line(ca,385,930+yShift,695,930+yShift,d.accent,3);
            headline(ca,model.message,540,1060+yShift,40,textColor,865);
        }
        // For stories, use lower footer to fill the additional height; no clipped artwork
        if(h==1920){line(ca,370,h-235,710,h-235,d.accent,2);small(ca,"UN RECUERDO QUE VIVE PARA SIEMPRE",540,h-170,24,textColor,false);}
        else small(ca,"✦",540,h-78,30,d.accent,true);
    }
    // Eight composition families with distinctly different portrait geometry.
    // All geometry uses the same 1080 px canvas and adapts to 4:5 or 9:16 output.
    private void renderReadyStatus(Canvas ca,int w,int h,Design d){
        int idx=d.motif,group=idx/6,shift=h==1920?0:-210;
        if(group==0){drawRibbon(ca,540,615+shift,370,d.accent);drawFlower(ca,840,790+shift,114,d.accent,Color.WHITE,idx);}
        else if(group==1){drawRibbon(ca,540,630+shift,250,d.accent);drawFlower(ca,235,645+shift,140,d.accent,Color.WHITE,idx);drawFlower(ca,815,840+shift,126,d.accent,Color.WHITE,idx);}
        else if(group==2){drawRibbon(ca,540,615+shift,290,d.accent);drawFlower(ca,840,825+shift,135,d.accent,Color.WHITE,idx);}
        else if(group==3){drawRibbon(ca,540,660+shift,290,d.accent);drawFlower(ca,245,820+shift,130,d.accent,Color.WHITE,idx);}
        else {drawRibbon(ca,540,600+shift,320,d.accent);drawFlower(ca,825,795+shift,128,d.accent,Color.WHITE,idx);}
        line(ca,285,1120+shift,795,1120+shift,d.accent,3);
        small(ca,"UN RECUERDO ESPECIAL",540,1195+shift,27,d.accent,true);
        headline(ca,model.date,540,1330+shift,67,d.ink,870);
        line(ca,320,1460+shift,760,1460+shift,d.accent,2);
        headline(ca,model.message,540,1600+shift,42,d.ink,845);
        if(!model.name.trim().isEmpty())headline(ca,model.name,540,1760+shift,36,d.ink,850);
    }
    private void renderPortraitCard(Canvas ca,int w,int h,Design d){
        int variant=d.motif, cx=540, cy=h==1920?715:500;
        boolean story=d.layout==5;
        if(variant==0){ // central archival portrait
            portraitFrame(ca,cx,cy,570,650,30,d.accent);
            drawRibbon(ca,835,cy+355,95,d.accent);
        }else if(variant==1){ // floral oval
            portraitEllipse(ca,cx,cy,560,685,d.accent);
            drawFlower(ca,220,cy-245,85,d.accent,Color.WHITE,1);
            drawFlower(ca,855,cy+245,100,d.accent,Color.WHITE,3);
        }else if(variant==2){ // generous square editorial layout
            portraitFrame(ca,cx,cy,770,580,16,d.accent);
            line(ca,130,cy-330,950,cy-330,d.accent,4);
        }else if(variant==3){ // split photostrip
            portraitFrame(ca,400,cy,590,720,22,d.accent);
            drawRibbon(ca,835,cy+35,145,d.accent);
        }else if(variant==4){ // light medallion
            portraitEllipse(ca,cx,cy,625,625,d.accent);
            drawFlower(ca,825,cy-275,100,d.accent,Color.WHITE,2);
        }else if(variant==5){ // panorama
            portraitFrame(ca,cx,cy-20,850,530,80,d.accent);
            drawFlower(ca,790,cy+270,95,d.accent,Color.WHITE,0);
        }else if(variant==6){ // ribbon corner photo
            portraitFrame(ca,cx,cy,690,760,28,d.accent);
            drawRibbon(ca,815,cy-270,100,d.accent);
        }else if(variant==7){ // remembrance frame
            portraitFrame(ca,cx,cy,625,700,120,d.accent);
            drawFlower(ca,260,cy+260,105,d.accent,Color.WHITE,2);
        }else if(variant==8){ // light-filled photographic portrait
            portraitFrame(ca,cx,cy-28,850,710,24,d.accent);
            portraitFade(ca,cx,cy+40,875,640,false);
            drawFlower(ca,160,cy+290,115,d.accent,Color.WHITE,0);
            drawFlower(ca,900,cy+275,105,d.accent,Color.WHITE,1);
        }else if(variant==9){ // celestial oval and soft gold light
            portraitEllipse(ca,cx,cy-45,690,725,d.accent);
            portraitFade(ca,cx,cy+55,770,600,false);
            drawFlower(ca,194,cy+270,108,d.accent,Color.WHITE,1);
            drawFlower(ca,878,cy+270,108,d.accent,Color.WHITE,2);
        }else if(variant==10){ // light ivory commemoration
            portraitFrame(ca,cx,cy-33,850,675,45,d.accent);
            portraitFade(ca,cx,cy+55,865,625,true);
            drawFlower(ca,167,cy+270,113,d.accent,Color.WHITE,3);
            drawFlower(ca,902,cy+265,90,d.accent,Color.WHITE,0);
        }else { // midnight garden
            portraitFrame(ca,cx,cy-25,775,705,28,d.accent);
            portraitFade(ca,cx,cy+55,802,615,false);
            drawFlower(ca,179,cy+275,115,d.accent,Color.WHITE,3);
            drawFlower(ca,899,cy+255,104,d.accent,Color.WHITE,0);
        }
        // Reserve a deliberately separated caption region, keeping faces uncluttered.
        float titleY=cy+(story?535:410);
        ornament(ca,540,(int)titleY-57,d.accent);
        small(ca,story?"SIEMPRE EN MI CORAZÓN":"EN TU MEMORIA",540,(int)titleY,30,d.accent,true);
        headline(ca,model.name,540,(int)titleY+95,52,d.ink,880);
        headline(ca,model.date,540,(int)titleY+182,35,d.ink,880);
        if(story)headline(ca,model.message,540,(int)titleY+290,31,d.ink,850);
        else headline(ca,model.message,540,(int)titleY+278,29,d.ink,850);
    }
    private void portraitFade(Canvas ca,int x,int y,int width,int height,boolean ivory){
        // Photo remains untouched; a compositional gradient prevents harsh cut-out edges.
        Paint veil=new Paint(3);
        int end=ivory?c("#F3EBDD"):c("#18171A");
        veil.setShader(new LinearGradient(x,y-height*.38f,x,y+height*.56f,
                new int[]{Color.TRANSPARENT,withAlpha(end,22),withAlpha(end,248)},
                new float[]{0f,.5f,1f},Shader.TileMode.CLAMP));
        ca.drawRect(x-width/2f,y-height*.38f,x+width/2f,y+height*.56f,veil);
    }
    private void portraitEllipse(Canvas ca,int x,int y,int width,int height,int accent){
        Path shape=new Path();shape.addOval(new RectF(x-width/2f,y-height/2f,x+width/2f,y+height/2f),Path.Direction.CW);
        portraitInShape(ca,shape,x,y,width,height,accent);
    }
    private void portraitFrame(Canvas ca,int x,int y,int width,int height,int radius,int accent){
        Path shape=new Path();shape.addRoundRect(new RectF(x-width/2f,y-height/2f,x+width/2f,y+height/2f),radius,radius,Path.Direction.CW);
        portraitInShape(ca,shape,x,y,width,height,accent);
    }
    private void portraitInShape(Canvas ca,Path clip,int x,int y,int width,int height,int accent){
        Paint p=new Paint(3);p.setColor(withAlpha(accent,170));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(9);ca.drawPath(clip,p);p.setStyle(Paint.Style.FILL);
        ca.save();ca.clipPath(clip);
        if(portrait!=null&&!portrait.isRecycled()){
            float zoom=Math.max(1f,Math.min(2.1f,model.zoom));
            float scale=Math.max(width/(float)portrait.getWidth(),height/(float)portrait.getHeight())*zoom;
            float bw=portrait.getWidth()*scale,bh=portrait.getHeight()*scale;
            float maxX=Math.max(0,(bw-width)/2),maxY=Math.max(0,(bh-height)/2);
            float dx=model.x/100f*maxX,dy=model.y/100f*maxY;
            p.setFilterBitmap(true);ca.drawBitmap(portrait,null,new RectF(x-bw/2+dx,y-bh/2+dy,x+bw/2+dx,y+bh/2+dy),p);
        }else{
            p.setShader(new LinearGradient(0,y-height/2,0,y+height/2,c("#D7D0C4"),c("#6C727B"),Shader.TileMode.CLAMP));
            ca.drawRect(x-width/2f,y-height/2f,x+width/2f,y+height/2f,p);p.setShader(null);
            p.setColor(withAlpha(Color.WHITE,160));ca.drawCircle(x,y-30,75,p);ca.drawOval(x-150,y+50,x+150,y+295,p);
            Paint tx=new Paint(3);tx.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));tx.setTextSize(28);tx.setColor(Color.WHITE);tx.setTextAlign(Paint.Align.CENTER);
            ca.drawText("AÑADIR FOTO",x,y+height/2f-46,tx);
        }
        ca.restore();
    }
    private static int withAlpha(int rgb,int a){return Color.argb(a,Color.red(rgb),Color.green(rgb),Color.blue(rgb));}
    private void line(Canvas c,float x,float y,float xx,float yy,int col,int width){Paint p=new Paint(3);p.setColor(col);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
    private void small(Canvas c,String str,int x,int y,int size,int col,boolean bold){Paint p=new Paint(3);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(bold?Typeface.create("sans-serif-medium",Typeface.NORMAL):Typeface.create("serif",Typeface.NORMAL));p.setTextSize(size);p.setColor(col);drawFitted(c,str,x,y,p,900);}
    private void headline(Canvas c,String txt,int x,int y,int sz,int color,int limit){
        Paint p=new Paint(3);p.setTextSize(sz);p.setColor(color);p.setTypeface(Typeface.create("serif",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);
        String[] words=txt.replace('\n',' ').trim().split("\\s+");ArrayList<String> lines=new ArrayList<>();String current="";
        for(String word:words){String test=current.isEmpty()?word:current+" "+word;if(p.measureText(test)>limit&&!current.isEmpty()){lines.add(current);current=word;}else current=test;}if(!current.isEmpty())lines.add(current);
        int maxRows=sz>=45?2:3;
        if(lines.size()>maxRows){lines=new ArrayList<>(lines.subList(0,maxRows));String last=lines.get(maxRows-1);lines.set(maxRows-1,last+"…");}
        int leading=(int)(sz*1.12);
        for(int i=0;i<lines.size();i++)drawFitted(c,lines.get(i),x,y+(i-(lines.size()-1)/2f)*leading,p,limit);
    }
    private void drawFitted(Canvas c,String text,float x,float y,Paint p,int width){float original=p.getTextSize();if(text==null||text.isEmpty())return;
        float measured=p.measureText(text);
        if(measured>width)p.setTextSize(Math.max(14f,original*width/measured));
        String visible=text;
        if(p.measureText(visible)>width){while(visible.codePointCount(0,visible.length())>1&&p.measureText(visible+"…")>width){
                visible=visible.substring(0,visible.offsetByCodePoints(visible.length(),-1));
            }visible+="…";}
        c.drawText(visible,x,y,p);p.setTextSize(original);}
    private void drawRibbon(Canvas ca,float x,float y,float size,int accent){
        ca.save();ca.translate(x,y);ca.scale(size/230f,size/230f);
        Paint shadow=new Paint(3);shadow.setColor(withAlpha(accent,165));shadow.setStyle(Paint.Style.STROKE);shadow.setStrokeWidth(10);Path ribbon=new Path();ribbon.moveTo(-55,-100);ribbon.cubicTo(-10,-170,105,-110,78,-29);ribbon.cubicTo(55,34,-51,134,-89,154);ribbon.lineTo(-124,111);ribbon.cubicTo(-33,38,54,-58,30,-91);ribbon.cubicTo(2,-125,-53,-92,-55,-49);ribbon.cubicTo(-55,-3,25,62,117,125);ribbon.lineTo(87,169);ribbon.cubicTo(-17,90,-120,14,-112,-56);ribbon.cubicTo(-108,-91,-82,-110,-55,-100);ribbon.close();ca.drawPath(ribbon,shadow);
        Paint fill=new Paint(3);fill.setShader(new LinearGradient(-115,-125,120,165,new int[]{c("#101217"),c("#363842"),c("#080A10"),c("#292B35")},null,Shader.TileMode.CLAMP));ca.drawPath(ribbon,fill);ca.restore();
    }
    // Lily with veined petals, five stamens and evergreen leaves; rendered without bitmap dependencies.
    private void drawFlower(Canvas ca,float cx,float cy,float radius,int gold,int white,int variant){
        ca.save();ca.translate(cx,cy);
        Paint p=new Paint(3);p.setColor(c("#53634C"));
        for(int k=0;k<3;k++){
            ca.save();ca.rotate(-75+k*73+variant*8);
            Path leaf=new Path();leaf.moveTo(0,radius*.24f);leaf.cubicTo(-radius*.55f,-radius*.32f,-radius*.48f,-radius*1.44f,0,-radius*1.60f);leaf.cubicTo(radius*.46f,-radius*1.06f,radius*.39f,-radius*.45f,0,radius*.24f);ca.drawPath(leaf,p);
            p.setColor(c("#A2B28A"));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);ca.drawLine(0,radius*.15f,0,-radius*1.32f,p);p.setStyle(Paint.Style.FILL);p.setColor(c("#53634C"));ca.restore();
        }
        for(int i=0;i<6;i++){
            ca.save();ca.rotate(i*60+variant*6);
            Path petal=new Path();petal.moveTo(0,0);petal.cubicTo(-radius*.45f,-radius*.18f,-radius*.48f,-radius*.83f,0,-radius*1.05f);petal.cubicTo(radius*.48f,-radius*.82f,radius*.45f,-radius*.18f,0,0);petal.close();
            p.setColor(i%2==0?c("#FFFCF3"):c("#EDE5D8"));ca.drawPath(petal,p);
            p.setColor(withAlpha(gold,130));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);ca.drawPath(petal,p);p.setStyle(Paint.Style.FILL);ca.restore();
        }
        p.setColor(c("#A98543"));p.setStrokeWidth(2.4f);for(int i=0;i<5;i++){
            double a=i*Math.PI*2/5;float xx=(float)Math.sin(a)*radius*.42f,yy=(float)Math.cos(a)*radius*.42f;
            ca.drawLine(0,0,xx,yy,p);ca.drawCircle(xx,yy,4.3f,p);
        }
        ca.restore();
    }
    private void ornament(Canvas ca,int x,int y,int accent){
        line(ca,x-235,y,x-60,y,withAlpha(accent,180),2);
        line(ca,x+60,y,x+235,y,withAlpha(accent,180),2);
        Paint p=new Paint(3);p.setColor(accent);Path sparkle=new Path();sparkle.moveTo(x,y-16);sparkle.lineTo(x+9,y);sparkle.lineTo(x,y+16);sparkle.lineTo(x-9,y);sparkle.close();ca.drawPath(sparkle,p);
    }
    private void drawPortrait(Canvas ca,int x,int y,int radius,int accent){if(portrait==null)return;Paint p=new Paint(3);p.setColor(withAlpha(accent,190));ca.drawCircle(x,y,radius+12,p);ca.save();Path mask=new Path();mask.addCircle(x,y,radius,Path.Direction.CW);ca.clipPath(mask);Bitmap bm=portrait;float scale=Math.max(2f*radius/bm.getWidth(),2f*radius/bm.getHeight());float ww=bm.getWidth()*scale,hh=bm.getHeight()*scale;RectF dest=new RectF(x-ww/2,y-hh/2,x+ww/2,y+hh/2);ca.drawBitmap(bm,null,dest,p);ca.restore();}
}
