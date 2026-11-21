package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.menu.LegacyDeskBookData;
import com.lulan.shincolle.menu.LegacyDeskState;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeskScreen extends DeskScreenSupport {
    private static final ResourceLocation guiTexture = texture("guidesk");
    private static final ResourceLocation guiRadar = texture("guideskradar");
    private static final ResourceLocation guiBook = texture("guideskbook");
    private static final ResourceLocation guiBook2 = texture("guideskbook2");
    private static final ResourceLocation guiTeam = texture("guideskteam");
    private static final ResourceLocation guiTarget = texture("guidesktarget");
    private static final ResourceLocation guiNameIcon0 = texture("guinameicon0");
    private static final ResourceLocation guiNameIcon1 = texture("guinameicon1");
    private static final ResourceLocation guiNameIcon2 = texture("guinameicon2");
    public int tickGUI;
    private int xClick, yClick, xMouse, yMouse, tempCD, lastXMouse, lastYMouse;
    private int guiFunc, type;
    private float GuiScale, GuiScaleInv;
    private int[] listNum, listClicked;
    private static final int CLICKCD = 60;
    private static final int LISTCLICK_RADAR = 0;
    private static final int LISTCLICK_TEAM = 1;
    private static final int LISTCLICK_TARGET = 2;
    private static final int LISTCLICK_ALLY = 3;
    private static final int LISTCLICK_BAN = 4;
    private static String StrPos, StrHeight, StrTeamID, StrBreak, StrAlly, StrOK, StrUnban,
                            StrBan, StrCancel, StrAllyList, StrBanList, StrRename, StrDisband, StrCreate,
                            StrNeutral, StrBelong, StrAllied, StrHostile, StrRemove;
    private int radar_zoomLv;
    private List<RadarEntity> shipList;
    private List<RadarEntity> itemList;
    private int book_chapNum;
    private int book_pageNum;
    private int teamState;
    private int listFocus;
    private DeskEditBox textField;
    private static final int TEAMSTATE_MAIN = 0;
    private static final int TEAMSTATE_CREATE = 1;
    private static final int TEAMSTATE_ALLY = 2;
    private static final int TEAMSTATE_RENAME = 3;
    private static final int TEAMSTATE_BAN = 4;
    Entity targetEntity = null;
    private ArrayList<String> tarList;
    private float mScale, mRotateX, mRotateY;
    DeskScreenSupport.Preview shipModel = null;
    DeskScreenSupport.PreviewMount shipMount = null;
    private int shipType, shipClass, shipStats, shipMaxStats;
    private int[][] iconXY = null;
    private final GuiBook GuiBook = new GuiBook();
    public DeskScreen(DeskMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320; this.imageHeight = 240;
        this.type = menu.getDeskType();
        this.GuiScale = 1.25F; this.GuiScaleInv = 0.8F;
        this.guiFunc = menu.getSelectedFunction();
        if (this.type == 0 && inventory.player.level().getBlockEntity(menu.getDeskPos()) instanceof DeskBlockEntity desk) {
            this.book_chapNum = desk.getBookChapter(); this.book_pageNum = desk.getBookPage();
            this.radar_zoomLv = desk.getRadarZoomIndex();
        }
        this.tempCD = CLICKCD;
        this.listNum = new int[5]; this.listClicked = new int[] {-1,-1,-1,-1,-1};
        this.shipList = new ArrayList<>(); this.itemList = new ArrayList<>();
        updateTargetClassList(); this.mScale = 30F; this.mRotateY = -30F;
        setShipModel(this.book_chapNum, this.book_pageNum);
        StrPos = I18n.get("gui.shincolle:radar.position");
        StrHeight = I18n.get("gui.shincolle:radar.height");
        StrTeamID = I18n.get("gui.shincolle:team.teamid");
        StrBreak = I18n.get("gui.shincolle:team.break");
        StrAlly = I18n.get("gui.shincolle:team.ally");
        StrOK = I18n.get("gui.shincolle:general.ok");
        StrUnban = I18n.get("gui.shincolle:team.unban");
        StrBan = I18n.get("gui.shincolle:team.ban");
        StrCancel = I18n.get("gui.shincolle:general.cancel");
        StrAllyList = I18n.get("gui.shincolle:team.allylist");
        StrBanList = I18n.get("gui.shincolle:team.banlist");
        StrRename = I18n.get("gui.shincolle:team.rename");
        StrDisband = I18n.get("gui.shincolle:team.disband");
        StrCreate = I18n.get("gui.shincolle:team.create");
        StrNeutral = I18n.get("gui.shincolle:team.neutral");
        StrBelong = I18n.get("gui.shincolle:team.belong");
        StrAllied = I18n.get("gui.shincolle:team.allied");
        StrHostile = I18n.get("gui.shincolle:team.hostile");
        StrRemove = I18n.get("gui.shincolle:target.remove");
    }
    @Override protected void init() {
        super.init();
        this.guiLeft = this.leftPos; this.guiTop = this.topPos;
        this.textField = new DeskEditBox((int)((guiLeft+10)*GuiScale), (int)((guiTop+24)*GuiScale), 153, 12);
        requestRadar();
    }
    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        this.graphics = g; this.xMouse = mx; this.yMouse = my;
        renderBackground(g);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1F, 1F, 1F, 1F);
        g.pose().pushPose(); g.pose().scale(GuiScale, GuiScale, 1F);
        drawGuiContainerBackgroundLayer(partial, mx, my);
        g.pose().pushPose(); g.pose().translate(guiLeft, guiTop, 0);
        drawGuiContainerForegroundLayer(mx, my);
        g.pose().popPose(); g.pose().popPose();
        this.textField.visible = this.teamState == TEAMSTATE_CREATE || this.teamState == TEAMSTATE_RENAME;
        if (this.textField.visible) this.textField.render(g, mx, my, partial);
        tickGUI++; if (tempCD > 0) tempCD--;
    }
    @Override protected void containerTick() {
        super.containerTick();
        if ((tickGUI & 63) == 0) requestRadar();
        textField.tick();
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        click((int)x, (int)y, button); return true;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        mouseClickMove((int)x, (int)y, button, 0); return true;
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        handleWheelMove(delta > 0); return true;
    }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (textField.visible && textField.isFocused() && key != 256) {
            textField.keyPressed(key, scan, mods); return true;
        }
        return super.keyPressed(key, scan, mods);
    }
    @Override public boolean charTyped(char c, int mods) {
        return textField.visible && textField.charTyped(c, mods);
    }
    private void setDeskFunction(int button) {
        if (button < 0 || button > 3) return;
        LegacyDeskState state = new LegacyDeskState(); state.function = guiFunc;
        state.selectFunction(button); guiFunc = state.function; syncTileEntityC2S();
    }
    private void updateDeskValue() {
        if (player.level().getBlockEntity(menu.getDeskPos()) instanceof DeskBlockEntity desk) {
            guiFunc = desk.getSelectedFunction(); book_chapNum = desk.getBookChapter();
            book_pageNum = desk.getBookPage(); radar_zoomLv = desk.getRadarZoomIndex();
        }
    }
    private void syncTileEntityC2S() {
        if (type == 0) syncDesk(guiFunc, radar_zoomLv, book_chapNum, book_pageNum);
    }
    private void updateTargetClassList() { this.tarList = new ArrayList<>(this.menu.getTargetClasses()); }
    private void getEntityByClick() {
        int selected = listNum[LISTCLICK_TARGET] + listClicked[LISTCLICK_TARGET];
        this.targetEntity = selected >= 0 && selected < tarList.size() ? createTarget(tarList.get(selected)) : null;
    }
    private void drawTargetModel() {
        if (targetEntity != null) renderModel(targetEntity, guiLeft+72, guiTop+136, mScale, mRotateX, mRotateY, 1F, null);
    }
    private void setShipModel(int chapter, int page) {
        this.shipMount = null;
        this.shipModel = preview(chapter, page);
        this.shipMaxStats = this.shipModel == null ? 0 : this.shipModel.modelButtons();
        if (shipModel != null) {
            this.shipType = shipModel.shipType(); this.shipClass = shipModel.ship.getShipClassId();
            this.iconXY = new int[][] {LegacyShipInventoryCatalog.typeIcon(shipType), LegacyShipInventoryCatalog.nameIcon(shipClass)};
        }
    }
    private void setShipMount() { this.shipMount = shipModel == null ? null : shipModel.mount(); }
    protected void drawGuiContainerBackgroundLayer(float par1,int par2, int par3)
    {
        GlStateManager.color(1F, 1F, 1F, 1F);
        bindTexture(guiTexture);
        if (this.type == 0)
        {
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 192);
            updateDeskValue();
            switch (this.guiFunc)
            {
            case 1:
                drawTexturedModalRect(guiLeft+3, guiTop+2, 0, 192, 16, 16);
            break;
            case 2:
                drawTexturedModalRect(guiLeft+22, guiTop+2, 16, 192, 16, 16);
            break;
            case 3:
                drawTexturedModalRect(guiLeft+41, guiTop+2, 32, 192, 16, 16);
            break;
            case 4:
                drawTexturedModalRect(guiLeft+60, guiTop+2, 48, 192, 16, 16);
            break;
            }
        }
        switch (this.guiFunc)
        {
        case 1:
            bindTexture(guiRadar);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 192);
            int texty = 192;
            switch (this.radar_zoomLv)
            {
            case 1:
                texty = 200;
            break;
            case 2:
                texty = 208;
            break;
            }
            drawTexturedModalRect(guiLeft+9, guiTop+160, 24, texty, 44, 8);
            if (this.listClicked[LISTCLICK_RADAR] > -1 && this.listClicked[LISTCLICK_RADAR] < 5)
            {
                int cirY = 25 + this.listClicked[LISTCLICK_RADAR] * 32;
                drawTexturedModalRect(guiLeft+142, guiTop+cirY, 68, 192, 108, 31);
            }
            drawRadarIcon();
            drawMoraleIcon();
        break;
        case 2:
            bindTexture(guiBook);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 192);
            if (xMouse < guiLeft + 137 * this.GuiScale)
            {
                drawTexturedModalRect(guiLeft+53, guiTop+182, 0, 192, 18, 10);
            }
            else
            {
                drawTexturedModalRect(guiLeft+175, guiTop+182, 0, 202, 18, 10);
            }
            if (this.book_pageNum > 0 && (this.book_chapNum == 4 || this.book_chapNum == 5))
            {
                drawShipModel();
            }
        break;
        case 3:
            bindTexture(guiTeam);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 192);
            drawTeamPic();
        break;
        case 4:
            bindTexture(guiTarget);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 192);
            if (this.listClicked[LISTCLICK_TARGET] > -1 && this.listClicked[LISTCLICK_TARGET] < 13)
            {
                int cirY = 25 + this.listClicked[LISTCLICK_TARGET] * 12;
                drawTexturedModalRect(guiLeft+142, guiTop+cirY, 68, 192, 108, 31);
            }
            drawTargetModel();
        break;
        }
    }

    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        switch (this.guiFunc)
        {
        case 1:
            drawRadarText();
        break;
        case 2:
            boolean canDrawText = true;
            if (this.book_pageNum > 0)
            {
                if (this.book_chapNum == 4 || this.book_chapNum == 5)
                {
                    if (this.shipModel == null)
                    {
                        canDrawText = false;
                    }
                    String str = "No. "+this.book_pageNum;
                    int strlen = (int)(fontRendererObj.getStringWidth(str) * 0.5F);
                    fontRendererObj.drawStringWithShadow(str, 55, 32, this.book_chapNum == 4 ? 11141120 : 65535);
                }
                else if (this.book_chapNum == 6)
                {
                }
            }
            if (canDrawText) GuiBook.drawBookContent(this, this.fontRendererObj, this.book_chapNum, this.book_pageNum);
        break;
        case 3:
            drawTeamText();
        break;
        case 4:
            drawTargetText();
        break;
        }
        handleHoveringText();
    }

    private void drawRadarIcon()
    {
        if (this.capa != null)
        {
            List<Integer> ships = capa.getShipEIDList();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(1, 1);
            Entity ship;
            double ox = 0D;
            double oy = 0D;
            double oz = 0D;
            int dx = 0;
            int dy = 0;
            int dz = 0;
            int id = 0;
            this.shipList = new ArrayList();
            this.itemList = new ArrayList();
            if (this.type == 0)
            {
                ox = this.menu.getDeskPos().getX();
                oy = this.menu.getDeskPos().getY();
                oz = this.menu.getDeskPos().getZ();
            }
            else
            {
                ox = this.player.getX();
                oy = this.player.getY();
                oz = this.player.getZ();
            }
            float radarScale = 1F;
            switch (this.radar_zoomLv)
            {
            case 1:
                radarScale = 2;
            break;
            case 2:
                radarScale = 4;
            break;
            }
            for (int eid : ships)
            {
                RadarEntity getent = null;
                double px = -1;
                double py = -1;
                double pz = -1;
                if (eid > 0)
                {
                    ship = this.player.level().getEntity(eid);
                    if (ship != null)
                    {
                        getent = new RadarEntity(ship);
                        px = ship.getX();
                        py = ship.getY();
                        pz = ship.getZ();
                    }
                }
                else
                {
                    continue;
                }
                if (py > 0)
                {
                    px -= ox;
                    py -= oy;
                    pz -= oz;
                    px *= radarScale;
                    py *= radarScale;
                    pz *= radarScale;
                    if ((int)px > 64) px = 64;
                    if ((int)px < -64) px = -64;
                    if ((int)pz > 64) pz = 64;
                    if ((int)pz < -64) pz = -64;
                    getent.pixelx = guiLeft + 69 + px;
                    getent.pixely = py;
                    getent.pixelz = guiTop + 88 + pz;
                    this.shipList.add(getent);
                    int sIcon = 0;
                    if (Mth.abs((int)px) > 48 || Mth.abs((int)pz) > 48)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 6) % 8 * 3;
                    }
                    else if (Mth.abs((int)px) > 32 || Mth.abs((int)pz) > 32)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 4) % 8 * 3;
                    }
                    else if (Mth.abs((int)px) > 16 || Mth.abs((int)pz) > 16)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 2) % 8 * 3;
                    }
                    else
                    {
                        sIcon = (int)(this.tickGUI * 0.125F) % 8 * 3;
                    }
                    if (id == this.listNum[LISTCLICK_RADAR] + this.listClicked[LISTCLICK_RADAR])
                    {
                        GlStateManager.color(1F, 0F, 0F, 1F);
                        drawTexturedModalRect(guiLeft+69+(int)px, guiTop+88+(int)pz, 0, 192, 3, 3);
                    }
                    else
                    {
                        GlStateManager.color(1F, 0.684F, 0.788F, 1F);
                        drawTexturedModalRect(guiLeft+69+(int)px, guiTop+88+(int)pz, sIcon, 192, 3, 3);
                    }
                    id++;
                }
            }
            if (itemCoordinates() != null && itemCoordinates().length > 1)
            {
                int entlen = itemCoordinates().length / 3;
                for (int i = 0; i < entlen; i++)
                {
                    double px = itemCoordinates()[i * 3];
                    double py = itemCoordinates()[i * 3 + 1];
                    double pz = itemCoordinates()[i * 3 + 2];
                    int posX = Mth.ceil(px);
                    int posY = Mth.ceil(py);
                    int posZ = Mth.ceil(pz);
                    px -= ox;
                    py -= oy;
                    pz -= oz;
                    px *= radarScale;
                    py *= radarScale;
                    pz *= radarScale;
                    if ((int)px > 64) px = 64;
                    if ((int)px < -64) px = -64;
                    if ((int)pz > 64) pz = 64;
                    if ((int)pz < -64) pz = -64;
                    RadarEntity getent = new RadarEntity(guiLeft + 69 + px, py, guiTop + 88 + pz,
                                                         posX, posY, posZ);
                    this.itemList.add(getent);
                    int sIcon = 0;
                    if (Mth.abs((int)px) > 48 || Mth.abs((int)pz) > 48)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 6) % 8 * 3;
                    }
                    else if (Mth.abs((int)px) > 32 || Mth.abs((int)pz) > 32)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 4) % 8 * 3;
                    }
                    else if (Mth.abs((int)px) > 16 || Mth.abs((int)pz) > 16)
                    {
                        sIcon = (int)(this.tickGUI * 0.125F + 2) % 8 * 3;
                    }
                    else
                    {
                        sIcon = (int)(this.tickGUI * 0.125F) % 8 * 3;
                    }
                    GlStateManager.color(0F, 1F, 1F, 1F);
                    drawTexturedModalRect(guiLeft+69+(int)px, guiTop+88+(int)pz, sIcon, 192, 3, 3);
                }
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableBlend();
        }
    }

    private void drawMoraleIcon()
    {
        bindTexture(guiNameIcon0);
        if (this.shipList != null)
        {
            int texty = 37;
            for (int i = this.listNum[LISTCLICK_RADAR]; i < shipList.size() && i < this.listNum[LISTCLICK_RADAR] + 5; ++i) {
                RadarEntity s = shipList.get(i);
                if (s != null && s.ship instanceof LegacyShipEntity)
                {
                    LegacyShipEntity s2 = (LegacyShipEntity) s.ship;
                    int ix = 44;
                    switch (DeskScreenSupport.moraleLevel(s2.getMorale()))
                    {
                    case 0:
                        ix = 0;
                    break;
                    case 1:
                        ix = 11;
                    break;
                    case 2:
                        ix = 22;
                    break;
                    case 3:
                        ix = 33;
                    break;
                    }
                    drawTexturedModalRect(guiLeft+237, guiTop+texty-1, ix, 240, 11, 11);
                }
                texty += 32;
            }
        }
    }

    private void drawRadarText()
    {
        String str, str2;
        RadarEntity s;
        LegacyShipEntity s2;
        int texty = 27;
        for (int i = this.listNum[LISTCLICK_RADAR]; i < shipList.size() && i < this.listNum[LISTCLICK_RADAR] + 5; ++i) {
            s = shipList.get(i);
            if (s != null && s.ship instanceof LegacyShipEntity)
            {
                s2 = (LegacyShipEntity) s.ship;
                fontRendererObj.drawString(s.name, 147, texty, 16777215);
                texty += 12;
                str = "LV " + ChatFormatting.YELLOW + s2.getShipLevel() + "   " +
                        ChatFormatting.GOLD + (int)s2.getHealth() + ChatFormatting.RED + " / " + (int)s2.getMaxHealth();
                str2 = StrPos + " " + ChatFormatting.YELLOW +
                      Mth.ceil(s.ship.getX()) + " , " +
                      Mth.ceil(s.ship.getZ()) + "  " + ChatFormatting.LIGHT_PURPLE +
                      StrHeight + " " + ChatFormatting.YELLOW +
                      (int)(s.ship.getY());
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.8F, 0.8F, 1F);
                fontRendererObj.drawString(str, 184, (int)(texty*1.25F), 65535);
                texty += 9;
                fontRendererObj.drawString(str2, 184, (int)(texty*1.25F), 16581630);
                texty += 11;
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawTeamPic()
    {
        int cirY = 0;
        if (this.listFocus == LISTCLICK_TEAM && this.listClicked[LISTCLICK_TEAM] > -1 && this.listClicked[LISTCLICK_TEAM] < 5)
        {
            cirY = 25 + this.listClicked[LISTCLICK_TEAM] * 32;
            drawTexturedModalRect(guiLeft+142, guiTop+cirY, 0, 192, 108, 31);
        }
        else if (this.listFocus == LISTCLICK_ALLY && this.listClicked[LISTCLICK_ALLY] > -1 && this.listClicked[LISTCLICK_ALLY] < 3)
        {
            cirY = 61 + this.listClicked[LISTCLICK_ALLY] * 31;
            drawTexturedModalRect(guiLeft+6, guiTop+cirY, 109, 192, 129, 31);
        }
        else if (this.listFocus == LISTCLICK_BAN && this.listClicked[LISTCLICK_BAN] > -1 && this.listClicked[LISTCLICK_BAN] < 3)
        {
            cirY = 61 + this.listClicked[LISTCLICK_BAN] * 31;
            drawTexturedModalRect(guiLeft+6, guiTop+cirY, 109, 192, 129, 31);
        }
    }

    private void drawTeamText()
    {
        if (this.capa == null) return;
        String str = null;
        TeamData tdata = null;
        if (this.capa.hasTeam())
        {
            tdata = this.capa.getPlayerTeamData(this.capa.getPlayerUID());
            if (tdata != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.8F, 0.8F, 0.8F);
                str = ChatFormatting.GRAY + StrTeamID +":  "+
                      ChatFormatting.YELLOW + this.capa.getPlayerUID() +" : "+
                      ChatFormatting.LIGHT_PURPLE + tdata.getLeaderName();
                fontRendererObj.drawString(str, 11, 34, 0);
                str = ChatFormatting.WHITE + tdata.getTeamName();
                fontRendererObj.drawSplitString(str, 11, 44, 160, 0);
                GlStateManager.popMatrix();
            }
        }
        String strLT = null;
        String strLB = null;
        String strRT = null;
        String strRB = null;
        int colorLT = 16777215;
        int colorLB = colorLT;
        int colorRT = colorLT;
        int colorRB = colorLT;
        switch (this.teamState)
        {
        case TEAMSTATE_ALLY:
            if (this.tempCD > 0)
            {
                strLT = String.valueOf((int)(tempCD * 0.05F));
                colorLT = 11184810;
            }
            else
            {
                int clicki = -1;
                List tlist = null;
                clicki = listClicked[LISTCLICK_TEAM] + this.listNum[LISTCLICK_TEAM];
                tlist = this.capa.getPlayerTeamDataList();
                if (this.listFocus == LISTCLICK_TEAM && tlist != null && clicki >= 0 && clicki < tlist.size())
                {
                    TeamData getd = (TeamData) tlist.get(clicki);
                    if (getd != null)
                    {
                        if (this.capa.getPlayerUID() != getd.getTeamId() &&
                            this.capa.isTeamAlly(getd.getTeamId()))
                        {
                            strLT = StrBreak;
                            colorLT = 16776960;
                        }
                        else
                        {
                            strLT = StrAlly;
                            colorLT = 65535;
                        }
                    }
                }
                else if (this.listFocus == LISTCLICK_ALLY)
                {
                    clicki = listClicked[LISTCLICK_ALLY] + this.listNum[LISTCLICK_ALLY];
                    tlist = this.capa.getPlayerTeamAllyList();
                    if (tlist != null && clicki >= 0 && clicki < tlist.size())
                    {
                        strLT = StrBreak;
                        colorLT = 16776960;
                    }
                }
            }
            strLB = StrOK;
            colorLB = 16777215;
        break;
        case TEAMSTATE_BAN:
            if (this.tempCD > 0)
            {
                strLT = String.valueOf((int)(tempCD * 0.05F));
                colorLT = 11184810;
            }
            else
            {
                int clicki2 = -1;
                List tlist2 = null;
                clicki2 = listClicked[LISTCLICK_TEAM] + this.listNum[LISTCLICK_TEAM];
                tlist2 = this.capa.getPlayerTeamDataList();
                if (this.listFocus == LISTCLICK_TEAM && tlist2 != null && clicki2 >= 0 && clicki2 < tlist2.size())
                {
                    TeamData getd = (TeamData) tlist2.get(clicki2);
                    if (getd != null)
                    {
                        if (this.capa.getPlayerUID() != getd.getTeamId() &&
                            this.capa.isTeamBanned(getd.getTeamId()))
                        {
                            strLT = StrUnban;
                            colorLT = 65535;
                        }
                        else
                        {
                            strLT = StrBan;
                            colorLT = 16776960;
                        }
                    }
                }
                else if (this.listFocus == LISTCLICK_BAN)
                {
                    clicki2 = listClicked[LISTCLICK_BAN] + this.listNum[LISTCLICK_BAN];
                    tlist2 = this.capa.getPlayerTeamBannedList();
                    if (tlist2 != null && clicki2 >= 0 && clicki2 < tlist2.size())
                    {
                        strLT = StrUnban;
                        colorLT = 65535;
                    }
                }
            }
            strLB = StrOK;
            colorLB = 16777215;
            break;
        case TEAMSTATE_CREATE:
            str = ChatFormatting.WHITE + StrTeamID +"  "+
                  ChatFormatting.YELLOW + this.capa.getPlayerUID();
            fontRendererObj.drawString(str, 10, 43, 0);
            strLB = StrOK;
            colorLB = 16777215;
            strLT = StrCancel;
            colorLT = 11184810;
        break;
        case TEAMSTATE_RENAME:
            strLB = StrOK;
            colorLB = 16777215;
            strLT = StrCancel;
            colorLT = 11184810;
        break;
        default:
            if (this.capa.hasTeam())
            {
                strLT = StrAllyList;
                colorLT = 65535;
                strLB = StrBanList;
                colorLB = 16776960;
                if (this.tempCD > 0)
                {
                    strRT = String.valueOf((int)(tempCD * 0.05F));
                    colorRT = 11184810;
                }
                else
                {
                    strRT = StrRename;
                    colorRT = 16777215;
                }
                if (this.capa.getPlayerTeamCooldown() > 0)
                {
                    strRB = String.valueOf(this.capa.getPlayerTeamCooldownInSec());
                    colorRB = 11184810;
                }
                else
                {
                    strRB = StrDisband;
                    colorRB = 3158064;
                }
            }
            else
            {
                if (this.capa.getPlayerTeamCooldown() > 0)
                {
                    strRB = String.valueOf(this.capa.getPlayerTeamCooldownInSec());
                    colorRB = 11184810;
                }
                else
                {
                    strRB = StrCreate;
                    colorRB = 65535;
                }
            }
        break;
        }
        int strlen = (int) (this.fontRendererObj.getStringWidth(strLT) * 0.5F);
        fontRendererObj.drawString(strLT, 31-strlen, 160, colorLT);
        strlen = (int) (this.fontRendererObj.getStringWidth(strLB) * 0.5F);
        fontRendererObj.drawString(strLB, 31-strlen, 174, colorLB);
        strlen = (int) (this.fontRendererObj.getStringWidth(strRT) * 0.5F);
        fontRendererObj.drawString(strRT, 110-strlen, 160, colorRT);
        strlen = (int) (this.fontRendererObj.getStringWidth(strRB) * 0.5F);
        fontRendererObj.drawString(strRB, 110-strlen, 174, colorRB);
        List<TeamData> tlist = this.capa.getPlayerTeamDataList();
        int texty = 33;
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
        for (int i = this.listNum[LISTCLICK_TEAM]; i < tlist.size() && i < this.listNum[LISTCLICK_TEAM] + 5; ++i)
        {
            TeamData tdata2 = tlist.get(i);
            if (tdata2 != null)
            {
                String allyInfo = ChatFormatting.WHITE +"("+ StrNeutral +")";
                if (this.capa.getPlayerUID() == tdata2.getTeamId())
                {
                    allyInfo = ChatFormatting.GOLD +"("+ StrBelong +")";
                }
                else if (this.capa.isTeamAlly(tdata2.getTeamId()))
                {
                    allyInfo = ChatFormatting.AQUA +"("+ StrAllied +")";
                }
                else if (this.capa.isTeamBanned(tdata2.getTeamId()))
                {
                    allyInfo = ChatFormatting.RED +"("+ StrHostile +")";
                }
                str = ChatFormatting.YELLOW +""+ tdata2.getTeamId() +" : "+
                      ChatFormatting.LIGHT_PURPLE + tdata2.getLeaderName() +"  "+
                      allyInfo;
                fontRendererObj.drawString(str, 181, texty, 16777215);
                texty += 9;
                fontRendererObj.drawSplitString(tdata2.getTeamName(), 181, texty, 132, 16777215);
                texty += 31;
            }
            else
            {
                texty += 40;
            }
        }
        GlStateManager.popMatrix();
        List<Integer> tlist3 = null;
        texty = 79;
        int listID = LISTCLICK_ALLY;
        if (tdata != null)
        {
            if (this.teamState == TEAMSTATE_ALLY)
            {
                tlist3 = tdata.getAllies();
                listID = LISTCLICK_ALLY;
            }
            else if (this.teamState == TEAMSTATE_BAN)
            {
                tlist3 = tdata.getBanned();
                listID = LISTCLICK_BAN;
            }
            if (tlist3 != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.8F, 0.8F, 0.8F);
                for (int i = this.listNum[listID]; i < tlist3.size() && i < this.listNum[listID] + 3; ++i)
                {
                    int getid = tlist3.get(i);
                    TeamData tdata3 = this.capa.getPlayerTeamData(getid);
                    if (tdata3 != null)
                    {
                        String allyInfo = ChatFormatting.WHITE +"("+ StrNeutral +")";
                        if (this.capa.getPlayerUID() == tdata3.getTeamId())
                        {
                            allyInfo = ChatFormatting.GOLD +"("+ StrBelong +")";
                        }
                        else if (this.capa.isTeamAlly(tdata3.getTeamId()))
                        {
                            allyInfo = ChatFormatting.AQUA +"("+ StrAllied +")";
                        }
                        else if (this.capa.isTeamBanned(tdata3.getTeamId()))
                        {
                            allyInfo = ChatFormatting.RED +"("+ StrHostile +")";
                        }
                        str = ChatFormatting.GRAY + StrTeamID +":  "+
                              ChatFormatting.YELLOW + tdata3.getTeamId() +" : "+
                              ChatFormatting.LIGHT_PURPLE + tdata3.getLeaderName() +"  "+
                              allyInfo;
                        fontRendererObj.drawString(str, 11, texty, 0);
                        texty += 9;
                        fontRendererObj.drawSplitString(tdata3.getTeamName(), 11, texty, 170, 16777215);
                        texty += 30;
                    }
                    else
                    {
                        texty += 39;
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawTargetText()
    {
        String str = StrRemove;
        int strlen = (int) (this.fontRendererObj.getStringWidth(str) * 0.5F);
        fontRendererObj.drawString(str, 31-strlen, 160, 16777215);
        int texty = 28;
        for (int i = this.listNum[LISTCLICK_TARGET]; i < tarList.size() && i < this.listNum[LISTCLICK_TARGET] + 13; ++i)
        {
            str = tarList.get(i);
            if (str != null)
            {
                fontRendererObj.drawString(str, 146, texty, 16777215);
                texty += 12;
            }
        }
    }

    private void handleClickTeamMain(int btn)
    {
        switch (btn)
        {
        case 0:
            if (this.capa.hasTeam())
            {
                this.teamState = TEAMSTATE_ALLY;
            }
        break;
        case 1:
            if (this.capa.hasTeam())
            {
                this.teamState = TEAMSTATE_BAN;
            }
        break;
        case 2:
            if (this.tempCD > 0)
            {
                break;
            }
            else if (this.capa.hasTeam())
            {
                this.teamState = TEAMSTATE_RENAME;
            }
        break;
        case 3:
            if (this.capa.getPlayerTeamCooldown() <= 0)
            {
                if (this.capa.hasTeam())
                {
                    send(GameplayCommandType.DESK_DISBAND_TEAM, "", 0);
                    this.teamState = TEAMSTATE_MAIN;
                    this.tempCD = CLICKCD;
                }
                else
                {
                    this.teamState = TEAMSTATE_CREATE;
                }
            }
        break;
        }
    }

    private void handleClickTeamAlly(int btn)
    {
        switch (btn)
        {
        case 0:
            if (this.tempCD > 0)
            {
                break;
            }
            int clicki = -1;
            int getTeamID = 0;
            boolean isAlly = false;
            clicki = listClicked[LISTCLICK_TEAM] + this.listNum[LISTCLICK_TEAM];
            List tlist = this.capa.getPlayerTeamDataList();
            if (this.listFocus == LISTCLICK_TEAM && tlist != null && clicki >= 0 && clicki < tlist.size())
            {
                TeamData getd = (TeamData) tlist.get(clicki);
                if (getd != null)
                {
                    getTeamID = getd.getTeamId();
                    if (this.capa.isTeamAlly(getTeamID))
                    {
                        isAlly = true;
                    }
                }
            }
            else if (this.listFocus == LISTCLICK_ALLY)
            {
                clicki = listClicked[LISTCLICK_ALLY] + this.listNum[LISTCLICK_ALLY];
                tlist = this.capa.getPlayerTeamAllyList();
                if (tlist != null && clicki >= 0 && clicki < tlist.size())
                {
                    getTeamID = (Integer) tlist.get(clicki);
                    isAlly = true;
                }
            }
            if (getTeamID > 0)
            {
                if (isAlly)
                {
                    send(GameplayCommandType.DESK_REMOVE_ALLY, "RelationTeamId", getTeamID);
                }
                else
                {
                    send(GameplayCommandType.DESK_ADD_ALLY, "RelationTeamId", getTeamID);
                }
                this.tempCD = CLICKCD;
            }
        break;
        case 1:
            this.teamState = TEAMSTATE_MAIN;
        break;
        }
    }

    private void handleClickTeamCreate(int btn)
    {
        switch (btn)
        {
        case 0:
            this.teamState = TEAMSTATE_MAIN;
        break;
        case 1:
            if (!this.capa.hasTeam())
            {
                String str = this.textField.getText();
                if (str != null && str.length() > 1)
                {
                    send(GameplayCommandType.DESK_CREATE_TEAM, "TeamName", str);
                    this.teamState = TEAMSTATE_MAIN;
                    this.tempCD = CLICKCD;
                }
            }
        break;
        }
    }

    private void handleClickTeamRename(int btn)
    {
        switch (btn)
        {
        case 0:
            this.teamState = TEAMSTATE_MAIN;
        break;
        case 1:
            if (this.capa.hasTeam())
            {
                String str = this.textField.getText();
                if (str != null && str.length() > 1)
                {
                    send(GameplayCommandType.DESK_RENAME_TEAM, "TeamName", str);
                    this.teamState = TEAMSTATE_MAIN;
                    this.tempCD = CLICKCD;
                }
            }
        break;
        }
    }

    private void handleClickTeamBan(int btn)
    {
        switch (btn)
        {
        case 0:
            if (this.tempCD > 0)
            {
                break;
            }
            int clicki = -1;
            int getTeamID = 0;
            boolean isBanned = false;
            clicki = listClicked[LISTCLICK_TEAM] + this.listNum[LISTCLICK_TEAM];
            List tlist = this.capa.getPlayerTeamDataList();
            if (this.listFocus == LISTCLICK_TEAM && tlist != null && clicki >= 0 && clicki < tlist.size())
            {
                TeamData getd = (TeamData) tlist.get(clicki);
                if (getd != null)
                {
                    getTeamID = getd.getTeamId();
                    if (this.capa.isTeamBanned(getTeamID))
                    {
                        isBanned = true;
                    }
                }
            }
            else if (this.listFocus == LISTCLICK_BAN)
            {
                clicki = listClicked[LISTCLICK_BAN] + this.listNum[LISTCLICK_BAN];
                tlist = this.capa.getPlayerTeamBannedList();
                if(tlist != null && clicki >= 0 && clicki < tlist.size())
                {
                    getTeamID = (Integer) tlist.get(clicki);
                    isBanned = true;
                }
            }
            if (getTeamID > 0)
            {
                if (isBanned)
                {
                    send(GameplayCommandType.DESK_REMOVE_BANNED, "RelationTeamId", getTeamID);
                }
                else
                {
                    send(GameplayCommandType.DESK_ADD_BANNED, "RelationTeamId", getTeamID);
                }
                this.tempCD = CLICKCD;
            }
        break;
        case 1:
            this.teamState = TEAMSTATE_MAIN;
        break;
        }
    }

    private void handleWheelMove(boolean isWheelUp)
    {
        int listSize = 0;
        int listID = -1;
        switch (this.guiFunc)
        {
        case 1:
            listSize = this.shipList.size();
            listID = LISTCLICK_RADAR;
        break;
        case 2:
            if (isWheelUp)
            {
                this.mScale += 2F;
                if(this.mScale > 200F) this.mScale = 200F;
            }
            else
            {
                this.mScale -= 2F;
                if(this.mScale < 5F) this.mScale = 5F;
            }
        break;
        case 3:
            if (xMouse - guiLeft > 138)
            {
                if (this.capa.getPlayerTeamDataMap() != null)
                {
                    listSize = this.capa.getPlayerTeamDataMap().size();
                    listID = LISTCLICK_TEAM;
                }
            }
            else
            {
                if (this.teamState == TEAMSTATE_ALLY && this.capa.getPlayerTeamAllyList() != null)
                {
                    listSize = this.capa.getPlayerTeamAllyList().size();
                    listID = LISTCLICK_ALLY;
                }
                else if (this.teamState == TEAMSTATE_BAN && this.capa.getPlayerTeamBannedList() != null)
                {
                    listSize = this.capa.getPlayerTeamBannedList().size();
                    listID = LISTCLICK_BAN;
                }
            }
        break;
        case 4:
            if (xMouse - guiLeft > 190)
            {
                listSize = this.tarList.size();
                listID = LISTCLICK_TARGET;
            }
            else
            {
                if (isWheelUp)
                {
                    this.mScale += 4F;
                    if(this.mScale > 200F) this.mScale = 200F;
                }
                else
                {
                    this.mScale -= 4F;
                    if(this.mScale < 5F) this.mScale = 5F;
                }
            }
        break;
        }
        if (listID < 0) return;
        if (isWheelUp)
        {
            listClicked[listID]++;
            listNum[listID]--;
            if (listNum[listID] < 0)
            {
                listNum[listID] = 0;
                listClicked[listID]--;
            }
        }
        else
        {
            if (listSize > 0)
            {
                listClicked[listID]--;
                listNum[listID]++;
                if (listNum[listID] > listSize - 1)
                {
                    listNum[listID] = listSize - 1;
                    listClicked[listID]++;
                }
                if (listNum[listID] < 0)
                {
                    listNum[listID] = 0;
                    listClicked[listID]++;
                }
            }
        }
    }

    protected void mouseClickMove(int mx, int my, int button, long time)
    {
        int dx = mx - this.lastXMouse;
        int dy = my - this.lastYMouse;
        this.lastXMouse = mx;
        this.lastYMouse = my;
        if (dx > 20 || dx < -20 || dy > 20 || dy < -20) return;
        int gx = (int) (mx * this.GuiScaleInv - this.guiLeft);
        int gy = (int) (my * this.GuiScaleInv - this.guiTop);
        if (this.guiFunc == 4 || this.guiFunc == 2)
        {
            if (gx > 8 && gx < 117 && gy > 47 && gy < 154)
            {
                if (dx > 0)
                {
                    this.mRotateY += dx * 3F;
                }
                if (dx < 0)
                {
                    this.mRotateY += dx * 3F;
                }
                if (dy > 0)
                {
                    this.mRotateX += dy * 2F;
                    if(this.mRotateX > 90F) this.mRotateX = 90F;
                }
                if (dy < 0)
                {
                    this.mRotateX += dy * 2F;
                    if(this.mRotateX < -90F) this.mRotateX = -90F;
                }
            }
        }
    }

    private void handleHoveringText()
    {
        int x2 = (int) (xMouse * this.GuiScaleInv);
        int y2 = (int) (yMouse * this.GuiScaleInv);
        int mx = x2 - guiLeft;
        int my = y2 - guiTop;
        switch (this.guiFunc)
        {
        case 1:
            List list = new ArrayList();
            for (RadarEntity obj : this.shipList)
            {
                Entity ship = null;
                if (obj != null)
                {
                    if (x2 < obj.pixelx+4 && x2 > obj.pixelx-2 &&
                        y2 < obj.pixelz+4 && y2 > obj.pixelz-2)
                    {
                        ship = obj.ship;
                    }
                }
                if (ship != null)
                {
                    String strName = obj.name;
                    list.add(strName);
                }
            }
            for (RadarEntity obj : this.itemList)
            {
                if (obj != null)
                {
                    if (x2 < obj.pixelx+4 && x2 > obj.pixelx-2 &&
                        y2 < obj.pixelz+4 && y2 > obj.pixelz-2)
                    {
                        String strCoord = String.valueOf(obj.posX) + ", " +
                                          String.valueOf(obj.posY) + ", " +
                                          String.valueOf(obj.posZ);
                        list.add(strCoord);
                    }
                }
            }
            drawHoveringText(list, mx, my, this.fontRendererObj);
        break;
        case 2:
            int getbtn = LegacyDeskState.buttonAt(2, mx, my);
            if (getbtn > 1 && getbtn < 9)
            {
                getbtn -= 2;
                String strChap = I18n.get("gui.shincolle:book.chap"+getbtn+".title");
                List list2 = CalcHelper.stringConvNewlineToList(strChap);
                int strLen = this.fontRendererObj.getStringWidth(strChap);
                drawHoveringText(list2, mx-strLen-20, my, this.fontRendererObj);
            }
            else
            {
                int id = GuiBook.getIndexID(this.book_chapNum, this.book_pageNum);
                List<int[]> cont = LegacyDeskBookData.BookList.get(id);
                if (cont != null)
                {
                    for (int[] getc : cont)
                    {
                        if (getc != null && getc.length == 5 && getc[0] == 2 &&
                            (getc[1] == GuiBook.PageLeftCurrent || getc[1] == GuiBook.PageRightCurrent))
                        {
                            int xa = getc[2] + GuiBook.Page0LX - 1;
                            if ((getc[1] & 1) == 1) xa = getc[2] + GuiBook.Page0RX - 1;
                            int xb = xa + 16;
                            int ya = getc[3] + GuiBook.Page0Y;
                            int yb = ya + 16;
                            ItemStack item = GuiBook.getItemStackForIcon(getc[4]);
                            if (mx > xa-1 && mx < xb+1 && my > ya-1 && my < yb+1)
                            {
                                this.renderToolTip(item, mx, my);
                                break;
                            }
                        }
                    }
                }
            }
        break;
        }
    }

    private void openShipGUI()
    {
        int clickid = this.listNum[LISTCLICK_RADAR] + this.listClicked[LISTCLICK_RADAR];
        if (clickid >= 0 && clickid < this.shipList.size())
        {
            Entity ent = this.shipList.get(clickid).ship;
            if (ent instanceof LegacyShipEntity)
            {
                this.minecraft.player.closeContainer();
                send(GameplayCommandType.OPEN_SHIP_INVENTORY, "ShipId", ent.getId());
            }
        }
    }

    private void click(int posX, int posY, int mouseKey)
    {
        this.textField.mouseClicked(posX, posY, mouseKey);
        posX = (int)(posX * this.GuiScaleInv);
        posY = (int)(posY * this.GuiScaleInv);
        xClick = posX - this.guiLeft;
        yClick = posY - this.guiTop;
        if (this.type == 0)
        {
            int getFunc = LegacyDeskState.buttonAt(0, xClick, yClick);
            setDeskFunction(getFunc);
        }
        switch (this.guiFunc)
        {
        case 1:
            int radarBtn = LegacyDeskState.buttonAt(1, xClick, yClick);
            switch (radarBtn)
            {
            case 0:
                this.radar_zoomLv++;
                if (this.radar_zoomLv > 2) this.radar_zoomLv = 0;
            break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                int oldClick = this.listClicked[LISTCLICK_RADAR];
                this.listClicked[LISTCLICK_RADAR] = radarBtn - 1;
                if (LegacyDeskState.opensRadarInventory(listNum[0]+oldClick, listNum[0]+listClicked[0], shipList.size())) this.openShipGUI();
            break;
            }
        break;
        case 2:
            int getbtn2 = -1;
            if (this.book_chapNum == 4 || this.book_chapNum == 5)
            {
                getbtn2 = LegacyDeskState.buttonAt(5, xClick, yClick);
                if (getbtn2 < 0 && xClick > 15 && xClick < 115 && yClick > 150 && yClick < 180)
                {
                    getbtn2 = 0;
                }
            }
            if(getbtn2 < 0)
            {
                int getbtn = LegacyDeskState.buttonAt(2, xClick, yClick);
                switch (getbtn)
                {
                case 0:
                    if (mouseKey == 0)
                    {
                        this.book_pageNum--;
                    }
                    else
                    {
                        this.book_pageNum -= 10;
                    }
                    if (this.book_pageNum < 0) this.book_pageNum = 0;
                    setShipModel(this.book_chapNum, this.book_pageNum);
                break;
                case 1:
                    if (mouseKey == 0)
                    {
                        this.book_pageNum++;
                    }
                    else
                    {
                        this.book_pageNum += 10;
                    }
                    if (this.book_pageNum > GuiBook.getMaxPageNumber(book_chapNum))
                    {
                        this.book_pageNum = GuiBook.getMaxPageNumber(book_chapNum);
                    }
                    setShipModel(this.book_chapNum, this.book_pageNum);
                break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    this.book_chapNum = getbtn - 2;
                    this.book_pageNum = 0;
                break;
                }
            }
            else
            {
                if (this.shipModel != null)
                {
                    switch (getbtn2)
                    {
                    case 0:
                    break;
                    case 1:
                        this.shipModel.setSitting(!this.shipModel.isSitting());
                        if (this.shipModel.getRNG().nextInt(2) == 0)
                        {
                            this.shipModel.setStateEmotion(1, 4, false);
                        }
                        else
                        {
                            this.shipModel.setStateEmotion(1, 0, false);
                        }
                        if (this.shipModel.getRNG().nextInt(2) == 0)
                        {
                            this.shipModel.setStateEmotion(7, 4, false);
                        }
                        else
                        {
                            this.shipModel.setStateEmotion(7, 0, false);
                        }
                    break;
                    case 2:
                        this.shipModel.setSprinting(!this.shipModel.isSprinting());
                        if (this.shipMount != null) this.shipMount.setSprinting(this.shipModel.isSprinting());
                    break;
                    case 3:
                        this.shipModel.setAttackTick(50);
                        this.shipModel.setStateEmotion(5, this.shipModel.getRNG().nextInt(4), false);
                        if (this.shipMount != null) this.shipMount.setAttackTick(50);
                    break;
                    case 4:
                        if (this.shipModel.getRNG().nextInt(2) == 0)
                        {
                            this.shipModel.setStateEmotion(7, 4, false);
                        }
                        else
                        {
                            this.shipModel.setStateEmotion(7, 0, false);
                        }
                        this.shipModel.setSneaking(this.shipModel.getRNG().nextInt(5) == 0);
                        if (this.shipModel.getRNG().nextInt(8) == 0)
                        {
                            this.shipModel.setStateFlag(2, true);
                        }
                        else
                        {
                            this.shipModel.setStateFlag(2, false);
                            this.shipModel.setStateEmotion(1, this.shipModel.getRNG().nextInt(10), false);
                            if (this.shipMount != null) this.shipMount.setStateEmotion(1, this.shipMount.getRNG().nextInt(10), false);
                        }
                    break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        this.shipModel.setStateEmotion(0, this.shipModel.getStateEmotion(0) ^ POW2[getbtn2-5], false);
                        if (this.shipModel.hasShipMounts()) this.setShipMount();
                    break;
                    }
                }
            }
        break;
        case 3:
            int teamBtn = LegacyDeskState.buttonAt(3, xClick, yClick);
            switch (teamBtn)
            {
            case 0:
                switch (this.teamState)
                {
                case TEAMSTATE_MAIN:
                    handleClickTeamMain(0);
                break;
                case TEAMSTATE_ALLY:
                    handleClickTeamAlly(0);
                break;
                case TEAMSTATE_BAN:
                    handleClickTeamBan(0);
                break;
                case TEAMSTATE_CREATE:
                    handleClickTeamCreate(0);
                break;
                case TEAMSTATE_RENAME:
                    handleClickTeamRename(0);
                break;
                }
            break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                this.listFocus = LISTCLICK_TEAM;
                this.listClicked[LISTCLICK_TEAM] = teamBtn - 1;
            break;
            case 6:
                switch (this.teamState)
                {
                case TEAMSTATE_MAIN:
                    handleClickTeamMain(1);
                break;
                case TEAMSTATE_ALLY:
                    handleClickTeamAlly(1);
                break;
                case TEAMSTATE_BAN:
                    handleClickTeamBan(1);
                break;
                case TEAMSTATE_CREATE:
                    handleClickTeamCreate(1);
                break;
                case TEAMSTATE_RENAME:
                    handleClickTeamRename(1);
                break;
                }
            break;
            case 7:
                switch (this.teamState)
                {
                case TEAMSTATE_MAIN:
                    handleClickTeamMain(2);
                break;
                }
                break;
            case 8:
                switch (this.teamState)
                {
                case TEAMSTATE_MAIN:
                    handleClickTeamMain(3);
                break;
                }
            break;
            case 9:
            case 10:
            case 11:
                switch (this.teamState)
                {
                case TEAMSTATE_ALLY:
                    this.listFocus = LISTCLICK_ALLY;
                    this.listClicked[LISTCLICK_ALLY] = teamBtn - 9;
                break;
                case TEAMSTATE_BAN:
                    this.listFocus = LISTCLICK_BAN;
                    this.listClicked[LISTCLICK_BAN] = teamBtn - 9;
                break;
                }
            break;
            }
        break;
        case 4:
            updateTargetClassList();
            int targetBtn = LegacyDeskState.buttonAt(4, xClick, yClick);
            switch (targetBtn)
            {
            case 0:
                int clicked = this.listNum[LISTCLICK_TARGET]+this.listClicked[LISTCLICK_TARGET];
                if (clicked >= 0 && clicked < this.tarList.size())
                {
                    String tarstr = this.tarList.get(clicked);
                    send(GameplayCommandType.TARGET_CLASS_REMOVE, "TargetClass", tarstr);
                }
            break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                this.listClicked[LISTCLICK_TARGET] = targetBtn - 1;
                getEntityByClick();
            break;
            }
        break;
        }
        syncTileEntityC2S();
    }

    private void drawShipModel()
    {
        if (this.shipModel != null)
        {
            bindTexture(guiBook2);
            if (this.book_chapNum == 4)
            {
                drawTexturedModalRect(guiLeft+20, guiTop+48, 0, 0, 87, 130);
            }
            else
            {
                drawTexturedModalRect(guiLeft+20, guiTop+48, 105, 0, 87, 130);
            }
            this.shipStats = this.shipModel.getStateEmotion(0);
            int numstats = 0;
            for (int i = 0; i < 8; i++)
            {
                if (numstats > this.shipMaxStats) break;
                for (int j = 0; j < 2; j++)
                {
                    if (++numstats > this.shipMaxStats) break;
                    if ((this.shipStats & POW2[numstats-1]) == POW2[numstats-1])
                    {
                        drawTexturedModalRect(guiLeft+45+i*9, guiTop+158+j*9, 115, 156, 7, 9);
                    }
                    else
                    {
                        drawTexturedModalRect(guiLeft+45+i*9, guiTop+158+j*9, 115, 147, 7, 9);
                    }
                }
            }
            try
            {
                bindTexture(guiNameIcon0);
                drawTexturedModalRect(guiLeft+23, guiTop+53, this.iconXY[0][0], this.iconXY[0][1], 28, 28);
                int offx = 0;
                int offy = 0;
                if (iconXY[1][0] < 100)
                {
                    bindTexture(guiNameIcon1);
                    if (iconXY[1][0] == 4) offy = -10;
                }
                else
                {
                    bindTexture(guiNameIcon2);
                    if (iconXY[1][0] == 6) offy = -10;
                    else offy = 10;
                }
                drawTexturedModalRect(guiLeft+30+offx, guiTop+94+offy, this.iconXY[1][1], this.iconXY[1][2], 11, 59);
            }
            catch (Exception e)
            {
            }
        int modelTicking = this.tickGUI % 3;
        if (modelTicking == 0) this.shipModel.advancePreview();
        renderModel(this.shipModel.ship, this.guiLeft + 72, this.guiTop + 110 + this.mScale * 1.1F,
                this.mScale, this.mRotateX, this.mRotateY, modelTicking * 0.33F, this.shipMount);
    } else {
        bindTexture(guiBook2);
        drawTexturedModalRect(guiLeft+20, guiTop+48, 0, 148, 87, 108);
    }
}
    private final class GuiBook
    {
        private final ResourceLocation guiBookPic01 = texture("book/bookpic01");
        private LegacyFont font;
        private int numChap;
        private int numPage;
        public int PageLeftCurrent = 0;
        public int PageRightCurrent = 0;
        public int PageWidth = 135;
        public int Page0LX = 13;
        public int Page0RX = 133;
        public int Page0Y = 48;
        public int PageTLX = 13;
        public int PageTRX = 162;
        public int PageTY = 58;
        public final int[] PageLimit = new int[] {1,28,6,20,26,19,4};
        public GuiBook() {}
        public void drawBookContent(DeskScreen par1, LegacyFont par2, int chap, int page)
        {
            int index = getIndexID(chap, page);
            List<int[]> cont = LegacyDeskBookData.BookList.get(index);
            font = par2;
            numChap = chap;
            numPage = page;
            if (cont == null)
            {
                cont =  Arrays.asList(new int[] {0, 0, 0, 0},
                                      new int[] {0, 1, 0, 0});
            }
            drawBookContent(cont);
        }
        private void drawBookContent(List<int[]> cont)
        {
            if (cont != null)
            {
                int leftRand = 0;
                int rightRand = 1;
                if ((tickGUI & 127) == 0)
                {
                    PageLeftCurrent += 2;
                    PageRightCurrent += 2;
                }
                for (int[] getc : cont)
                {
                    if (getc != null)
                    {
                        switch (getc[0])
                        {
                        case 0:
                            drawBookText(getc[1], getc[2], getc[3]);
                        break;
                        case 1:
                            if (PageLeftCurrent > leftRand) PageLeftCurrent = 0;
                            if (PageRightCurrent > rightRand) PageRightCurrent = 1;
                            if (getc[1] == PageLeftCurrent || getc[1] == PageRightCurrent)
                                drawBookPic(getc);
                        break;
                        case 2:
                            if (PageLeftCurrent > leftRand) PageLeftCurrent = 0;
                            if (PageRightCurrent > rightRand) PageRightCurrent = 1;
                            if (getc[1] == PageLeftCurrent || getc[1] == PageRightCurrent)
                                drawBookIcon(getc[1], getc[2], getc[3], getc[4]);
                        break;
                        case 3:
                            leftRand = getc[1];
                            rightRand = getc[2];
                        break;
                        }
                    }
                }
            }
        }
        private void drawBookText(int pageSide, int offX, int offY)
        {
            drawTitleText();
            drawPageText(pageSide, offX, offY);
        }
        private void drawTitleText()
        {
            String str = null;
            switch (numChap)
            {
            case 0:
                str = I18n.get("gui.shincolle:book.chap"+numChap+".title");
                break;
            default:
                str = I18n.get("gui.shincolle:book.chap"+numChap+".title"+numPage);
                break;
            }
            int strlen = (int) (font.getStringWidth(str) * 0.5F);
            str = ChatFormatting.UNDERLINE + str;
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.8F, 0.8F, 0.8F);
            font.drawString(str, 82-strlen, 40, 11141120);
            GlStateManager.popMatrix();
        }
        private void drawPageText(int pageSide, int offX, int offY)
        {
            int picY = PageTY + offY - 4;
            int picX = PageTLX;
            if(pageSide > 0) picX = PageTRX;
            picX += offX;
            String str = I18n.get("gui.shincolle:book.chap"+numChap+".text"+numPage+"d"+pageSide);
            drawStringWithSpecialSymbol(str, picX, picY);
        }
        private void drawStringWithSpecialSymbol(String str, int x, int y)
        {
            String[] strArray = str.split("<BR>|<BR/>|<br>|<br/>");
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.8F, 0.8F, 0.8F);
            int newY = y;
            for (String s : strArray)
            {
                font.drawSplitString(s, x, newY, PageWidth, 0);
                newY += font.splitStringWidth(s, PageWidth);
            }
            GlStateManager.popMatrix();
        }
        private void drawBookPic(int[] parms)
        {
            int pageSide, posX, posY, picID, picU, picV, sizeX, sizeY;
            if (parms != null && parms.length == 9)
            {
                pageSide = parms[1];
                posX = parms[2];
                posY = parms[3];
                picID = parms[4];
                picU = parms[5];
                picV = parms[6];
                sizeX = parms[7];
                sizeY = parms[8];
            }
            else
            {
                return;
            }
            int picY = Page0Y + posY;
            int picX = Page0LX;
            if ((pageSide & 1) == 1) picX = Page0RX;
            picX += posX;
            switch (picID)
            {
            case 0:
                bindTexture(guiBookPic01);
                break;
            case 1:
                break;
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(1, 1);
            drawTexturedModalRect(picX, picY, picU, picV, sizeX, sizeY);
            GlStateManager.disableBlend();
        }
        private void drawBookIcon(int pageSide, int offX, int offY, int iconID)
        {
            int picY = Page0Y + offY;
            int picX = Page0LX;
            if ((pageSide & 1) == 1) picX = Page0RX;
            picX += offX;
            drawItemIcon(getItemStackForIcon(iconID), picX, picY, false);
        }
        private void drawItemIcon(ItemStack item, int x, int y, boolean effect)
        {
            if (item != null)
            {
                graphics.renderItem(item, x, y);
            }
        }
        public int getMaxPageNumber(int chap)
        {
            if (chap < PageLimit.length) return PageLimit[chap];
            return 0;
        }
        public int getIndexID(int ch, int pg)
        {
            return ch * 1000 + pg;
        }
        public ItemStack getItemStackForIcon(int itemID)
        {
            return LegacyDeskBookData.icon(itemID);
        }
    }
}
