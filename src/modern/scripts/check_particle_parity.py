import hashlib
import pathlib
import re
import subprocess
import sys


legacy = pathlib.Path(sys.argv[1]) / "src/main"
output = pathlib.Path(sys.argv[2])
root = pathlib.Path(__file__).resolve().parents[3]
modern = root / "src/modern"
particles = modern / "java/com/lulan/shincolle/client/particle"
old = (legacy / "java/com/lulan/shincolle/client/particle/ParticleEmotion.java").read_text()
new = (particles / "LegacyEmotionParticle.java").read_text()


def method(source, signature):
    start = source.index(signature)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


renames = {
    "ParticleEmotion(": "LegacyEmotionParticle(", "World world": "ClientLevel world",
    "particleMaxAge": "lifetime", "particleAge": "age", "particleScale": "quadSize",
    "particleAlpha": "alpha", "this.rand": "this.random", "prevPosX": "xo",
    "prevPosY": "yo", "prevPosZ": "zo", "motionX": "xd", "motionY": "yd", "motionZ": "zd",
    "setPosition(": "setPos(", "setExpired()": "remove()", "canCollide": "hasPhysics",
    "onUpdate()": "tick()", "this.host.posX": "this.host.getX()",
    "this.host.posY": "this.host.getY()", "this.host.posZ": "this.host.getZ()",
    "this.posX": "this.x", "this.posY": "this.y", "this.posZ": "this.z",
    "ClientProxy.getClientPlayer().renderYawOffset": "Minecraft.getInstance().player.yBodyRot",
    "CalcHelper.rotateXZByAxis": "rotateXZByAxis",
}


def adapt(source):
    for before, after in renames.items():
        source = source.replace(before, after)
    return source


def normalized(source):
    source = re.sub(r"/\*.*?\*/|//[^\n]*", "", source, flags=re.S)
    return re.sub(r"\s+", "", source)


methods = []
for before, after in [
    ("public ParticleEmotion(", "public LegacyEmotionParticle("),
    ("public void onUpdate()", "public void tick()"),
    ("private void updateHostPosition()", "private void updateHostPosition()"),
    ("private void calcParticlePosition()", "private void calcParticlePosition()"),
]:
    translated = adapt(method(old, before))
    current = method(new, after)
    assert normalized(translated) == normalized(current), after
    methods.append(current)
print("Emotion mechanical parity: constructor (all 35/default cases), tick, host update, spawn position: 4/4")

for relative in ["textures/gui/GuiHUD.png", "textures/particles/ParticleEmotion.png",
                 "textures/particles/ParticleTexts.png"]:
    expected = legacy / "resources/assets/shincolle" / relative
    target = modern / "resources/assets/shincolle" / relative.lower()
    assert hashlib.sha256(expected.read_bytes()).digest() == hashlib.sha256(target.read_bytes()).digest(), relative
print("Original texture byte parity: 3/3")

fields = new[new.index("public static final int EMO_NUMBER"):new.index("public LegacyEmotionParticle(")]
text = (particles / "LegacyTextParticle.java").read_text()
java = """
import java.util.Random;
class ClientLevel {}
class Entity {
 double x=7, y=8, z=9; float yBodyRot=47;
 double getX(){return x;} double getY(){return y;} double getZ(){return z;}
}
class Minecraft {
 static final Minecraft MC=new Minecraft(); final Entity player=new Entity();
 static Minecraft getInstance(){return MC;}
}
class Values {static class N {static final float DIV_PI_180=0.0174532925F;}}
class Mth {
 static final float[] SIN=new float[65536];
 static {for(int i=0;i<65536;i++) SIN[i]=(float)Math.sin(i*Math.PI*2/65536);}
 static float sin(float x){return SIN[(int)(x*10430.378F)&65535];}
 static float cos(float x){return SIN[(int)(x*10430.378F+16384)&65535];}
}
class ParticleState {
 final Random random=new Random(42);
 double x,y,z,xo,yo,zo,xd,yd,zd; float quadSize,alpha=1; int age,lifetime;
 boolean hasPhysics,removed;
 ParticleState(ClientLevel l,double x,double y,double z){setPos(x,y,z);xo=x;yo=y;zo=z;}
 void setPos(double a,double b,double c){x=a;y=b;z=c;}
 void setSize(float a,float b){} void remove(){removed=true;}
 void move(double a,double b,double c){x+=a;y+=b;z+=c;}
}
class LegacyEmotionParticle extends ParticleState {
""" + fields + "\n".join(methods).replace("@Override", "") + method(new, "private static float[] rotateXZByAxis(") + """
 void checkFrame(){
  if(age<0)return;
  int frame=Math.min(age,lifetime);
  float v=particleIconY+frame*0.0625F;
  if(particleIconX<0 || particleIconX+0.0625F>1 || v<0 || v+0.0625F*frameSize>1)
   throw new AssertionError("invalid sprite UV");
 }
}
class LegacyTextParticle extends ParticleState {
 private final int particleType;
""" + method(text, "public LegacyTextParticle(") + method(text, "public void tick()") + """
}
public class ParticleStateCheck {
 public static void main(String[] args){
  int scenarios=0;
  for(int type=-1;type<=35;type++)for(int ent=-1;ent<=2;ent++)
   for(float height:new float[]{0F,0.5F,3F})for(boolean bound:new boolean[]{false,true}){
    Entity host=bound?new Entity():null;
    LegacyEmotionParticle p=new LegacyEmotionParticle(new ClientLevel(),host,1,2,3,height,ent,type);
    int ticks=0;
    while(!p.removed && ticks++<600){
     if(host!=null){host.x+=0.03;host.y+=0.02;host.z-=0.01;}
     p.tick();p.checkFrame();
     if(!Float.isFinite(p.alpha) || p.alpha<0 || p.alpha>1.20001F
       || !Double.isFinite(p.x+p.y+p.z)) throw new AssertionError("invalid animation state");
    }
    if(!p.removed)throw new AssertionError("animation did not expire: "+type);
    scenarios++;
   }
  for(int type=0;type<5;type++){
   LegacyTextParticle p=new LegacyTextParticle(new ClientLevel(),1,2,3,1,type);
   if(p.lifetime!=25 || p.yd!=0.1D || p.hasPhysics)throw new AssertionError("text init");
   for(int tick=0;tick<27;tick++){
    p.tick();if(tick<26 && p.removed)throw new AssertionError("text expired early");
   }
   if(!p.removed || Math.abs(p.yd-0.1D*Math.pow(0.9D,27))>1e-14)
    throw new AssertionError("text lifetime/motion");
  }
  System.out.println("Headless state checks (API stubs, not rendering): "+scenarios+" emotion scenarios; 5 text types: passed");
 }
}
"""
output.mkdir(parents=True, exist_ok=True)
source = output / "ParticleStateCheck.java"
source.write_text(java)
subprocess.run(["javac", "-d", str(output), str(source)], check=True)
subprocess.run(["java", "-cp", str(output), "ParticleStateCheck"], check=True)
