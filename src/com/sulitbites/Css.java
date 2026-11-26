package com.sulitbites;

/** The style sheet of the website (served at /style.css). */
final class Css {
    private Css() { }
    static final String TEXT = """
:root{--red:#e5322d;--yel:#ffb800;--ink:#17171c;--bg:#f6f6f4;--grn:#1fa463;--mut:#6b6b73;--sh:4px 4px 0 var(--ink)}
*{box-sizing:border-box;margin:0;padding:0}
body{font:16px/1.55 'DM Sans',system-ui,sans-serif;background:var(--bg);color:var(--ink)}
h1,h2,h3,.logo{font-family:'Bricolage Grotesque','DM Sans',system-ui,sans-serif;line-height:1.08;letter-spacing:-.02em}
a{color:inherit;text-decoration:none}
:focus-visible{outline:3px solid var(--red);outline-offset:2px}
.wrap{max-width:1140px;margin:0 auto}.sec{padding:56px 20px;max-width:1180px;margin:0 auto}
h2{font-size:clamp(28px,4vw,40px);font-weight:800}h3{font-size:21px;font-weight:700}.mut{color:var(--mut)}
.btn{display:inline-block;border:2px solid var(--ink);background:var(--red);color:#fff;font:700 15px 'DM Sans',sans-serif;padding:10px 18px;border-radius:12px;cursor:pointer;box-shadow:3px 3px 0 var(--ink);transition:transform .08s,box-shadow .08s;text-align:center}
.btn:active{transform:translate(3px,3px);box-shadow:none}
.btn.alt{background:#fff;color:var(--ink)}.btn.y{background:var(--yel);color:var(--ink)}
.btn.sm{padding:6px 12px;font-size:13px;box-shadow:2px 2px 0 var(--ink)}.btn.lg{padding:14px 26px;font-size:17px}.btn.full{width:100%}
.card{background:#fff;border:2px solid var(--ink);border-radius:16px;box-shadow:var(--sh)}
.in{width:100%;border:2px solid var(--ink);border-radius:10px;padding:10px 12px;font:inherit;background:#fff;margin:6px 0}.in.sm{padding:6px 10px;width:auto;margin:0}
.nav{position:sticky;top:0;z-index:20;background:#fff;border-bottom:2px solid var(--ink);display:flex;align-items:center;gap:26px;padding:12px 24px}
.logo{font-size:24px;font-weight:800;display:flex;align-items:center;gap:8px}.logo b{background:var(--yel);border:2px solid var(--ink);border-radius:10px;padding:0 6px;font-size:20px}
.nav nav{display:flex;gap:20px;font-weight:700}.nav nav a:hover{color:var(--red)}
.nr{margin-left:auto;display:flex;align-items:center;gap:12px}
.cnt{background:var(--yel);border:2px solid var(--ink);border-radius:99px;padding:0 8px;margin-left:6px}
.who{font-weight:700;font-size:14px;text-align:right;line-height:1.2}.who small{display:block;color:var(--mut);font-weight:400}
.hero{background:var(--yel);border-bottom:2px solid var(--ink)}
.hero-in{display:grid;grid-template-columns:1.1fr .9fr;gap:48px;align-items:center;padding:68px 24px}
.hero h1{font-size:clamp(46px,7.5vw,88px);font-weight:800}
.hero p{font-size:19px;margin:20px 0 28px;max-width:44ch}.cta{display:flex;gap:14px;flex-wrap:wrap}
.ticket{background:#fff;border:2px solid var(--ink);box-shadow:var(--sh);padding:24px;font-family:ui-monospace,Menlo,Consolas,monospace;transform:rotate(2deg);animation:print .9s ease-out both}
@keyframes print{from{transform:translateY(-50px) rotate(2deg);opacity:0}}
.tdash{border-top:2px dashed var(--ink);margin:12px 0}.tr,.r{display:flex;justify-content:space-between;gap:12px;padding:4px 0}
.tr.tot,.r.t{font-size:22px;font-weight:800}.ticket .btn{margin-top:16px}
.sh{display:flex;justify-content:space-between;align-items:end;gap:16px;flex-wrap:wrap;margin-bottom:24px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(235px,1fr));gap:24px}
.pc{overflow:hidden;display:flex;flex-direction:column}
.thumb{font-size:78px;text-align:center;padding:24px 0;border-bottom:2px solid var(--ink);background:var(--yel)}
.Drink .thumb,.cat-t.Drink{background:#9edbff}.Dessert .thumb,.cat-t.Dessert{background:#ffb3c7}.cat-t.Meal{background:var(--yel)}
.cb{padding:16px;display:flex;flex-direction:column;gap:6px;flex:1}.cb p{color:var(--mut);font-size:14px;flex:1}
.tagp{font-size:12px;font-weight:700;background:var(--ink);color:#fff;border-radius:6px;padding:1px 8px;align-self:flex-start}
.promo{color:var(--grn);font-weight:700;font-size:13px}.row{display:flex;justify-content:space-between;align-items:center;margin-top:8px}.price{font-size:21px;font-weight:800}
.g3{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:24px;margin-top:22px}
.cat-t{padding:28px;display:block;border:2px solid var(--ink);border-radius:16px;box-shadow:var(--sh)}.cat-t i{font-size:54px;font-style:normal;display:block}
.stp{padding:24px}.n{display:inline-grid;place-items:center;width:38px;height:38px;border-radius:50%;background:var(--red);color:#fff;font-weight:800;border:2px solid var(--ink);margin-bottom:12px}
.banner{background:var(--ink);color:#fff;border-radius:20px;padding:42px;display:flex;justify-content:space-between;align-items:center;gap:22px;flex-wrap:wrap}.banner p{color:#c9c9d0;margin-top:8px}
.pills{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:24px}.pill{border:2px solid var(--ink);background:#fff;border-radius:99px;padding:6px 18px;font:700 14px 'DM Sans',sans-serif;cursor:pointer}.pill.on{background:var(--ink);color:#fff}
.split{display:grid;grid-template-columns:1.6fr 1fr;gap:26px;align-items:start;margin-top:22px}
.line{display:flex;align-items:center;gap:14px;padding:14px 18px;border-bottom:2px dashed #d8d8d8}.line:last-child{border:0}
.em{font-size:36px}.li{flex:1}.li small{display:block;color:var(--mut)}
.qty{display:flex;align-items:center;gap:8px;border:2px solid var(--ink);border-radius:10px;padding:2px 6px}.qty button{border:0;background:none;font-size:18px;font-weight:800;cursor:pointer;width:24px}
.lt{min-width:84px;text-align:right}.x{border:0;background:none;cursor:pointer;font-size:17px}
.sum{padding:22px;position:sticky;top:92px}.sum h3{margin-bottom:8px}.sum label{display:block;margin-top:14px;font-weight:700}.sum .btn{margin-top:12px}
.r.t{border-top:2px dashed var(--ink);margin-top:8px;padding-top:12px}.r.g{color:var(--grn)}
.empty{text-align:center}.empty .big{font-size:72px}
.auth{display:grid;place-items:center;padding:60px 20px}.ac{width:min(440px,100%);padding:28px}.tabs{display:flex;gap:10px;margin-bottom:20px}.tabs .pill{flex:1}.hint{font-size:13px;color:var(--mut);margin-top:16px}
.oc{display:grid;grid-template-columns:1fr 2fr auto;gap:18px;align-items:center;padding:16px 22px;margin-top:18px}.oc small{display:block;color:var(--mut)}.or{display:flex;gap:12px;align-items:center;flex-wrap:wrap}
.bd{border:2px solid var(--ink);border-radius:99px;padding:1px 11px;font-size:12px;font-weight:700;background:#fff;white-space:nowrap}
.bd.Pending{background:#ffe08a}.bd.Preparing{background:#9edbff}.bd.Ready{background:#b7f0cf}.bd.Completed{background:var(--grn);color:#fff}.bd.Cancelled{background:#ffb3b0}
.adm{display:grid;grid-template-columns:190px 1fr;gap:28px;align-items:start}.side{padding:10px;display:flex;flex-direction:column;gap:4px}.side a{padding:10px 14px;border-radius:10px;font-weight:700}.side a.on{background:var(--ink);color:#fff}
.stats4{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:18px;margin:20px 0}.st{padding:16px 18px}.st span{display:block;color:var(--mut);font-size:14px}.st b{font-size:28px;font-family:'Bricolage Grotesque',sans-serif}
.two{display:grid;grid-template-columns:1fr 1fr;gap:22px}.pad{padding:20px}.mt{margin-top:28px}
.bar{display:grid;grid-template-columns:110px 1fr auto;gap:10px;align-items:center;margin:12px 0;font-size:14px}.bar div{background:#eee;border:2px solid var(--ink);border-radius:99px;height:14px;overflow:hidden}.bar i{display:block;height:100%;background:var(--red)}
.tw{overflow-x:auto}.tbl{width:100%;border-collapse:collapse;font-size:14px}.tbl th{background:var(--ink);color:#fff;text-align:left;padding:11px 14px}.tbl td{padding:11px 14px;border-bottom:1px solid #e6e6e6}.act{white-space:nowrap}
.modal{position:fixed;inset:0;background:rgba(23,23,28,.55);display:grid;place-items:center;z-index:50;padding:20px}.hidden{display:none}
.box{background:#fff;border:2px solid var(--ink);border-radius:16px;box-shadow:var(--sh);padding:26px;width:min(440px,100%);max-height:90vh;overflow:auto}.box h2{font-size:28px;margin-bottom:8px}.rc{font-family:ui-monospace,Menlo,Consolas,monospace}.two2{display:grid;grid-template-columns:1fr 1fr;gap:12px}
#toast{position:fixed;bottom:24px;left:50%;transform:translate(-50%,140px);background:var(--ink);color:#fff;padding:12px 20px;border-radius:12px;font-weight:700;transition:transform .25s;z-index:60;max-width:90vw}#toast.show{transform:translate(-50%,0)}#toast.err{background:var(--red)}
footer{background:var(--ink);color:#c9c9d0;text-align:center;padding:26px;font-size:14px}
@media(max-width:820px){.hero-in,.split,.adm,.two,.oc{grid-template-columns:1fr}.nav{flex-wrap:wrap;gap:10px}.nav nav{order:3;width:100%}.ticket{transform:none;animation:none}.side{flex-direction:row;overflow-x:auto}.sum{position:static}}
@media(prefers-reduced-motion:reduce){*{animation:none!important;transition:none!important}}

.rc{font-size:14px}.rh{text-align:center}.rlogo{font-size:34px;line-height:1.1}.rh h2{font-size:26px;margin:0}.rh p{font-size:12px;color:var(--mut);line-height:1.4}
.stamp{margin:12px auto;width:max-content;border:3px solid var(--red);color:var(--red);font-weight:800;font-size:22px;padding:2px 14px;transform:rotate(-5deg);letter-spacing:.08em}
.thx{text-align:center;font-size:12px;margin-top:8px;line-height:1.5}.rc .r span:last-child{text-align:right}
.oc.cx .oi{text-decoration:line-through;color:var(--mut)}.oc.cx b{color:var(--mut)}


.pc.out .thumb{filter:grayscale(1);opacity:.5}.oos{font-weight:700;color:var(--red)}.low{color:var(--red);font-weight:700;font-size:13px}
.tags{display:flex;gap:6px;align-items:center;flex-wrap:wrap}.av{font-size:12px;font-weight:700;border:2px solid var(--ink);border-radius:99px;padding:0 9px}.av-ok{background:#b7f0cf}.av-low{background:#ffe08a}.av-no{background:#ffb3b0}
.sk .skh{display:flex;gap:14px;align-items:center;margin-bottom:14px}.sk .skh h2{margin:0 0 4px;font-size:24px}.sk .em{font-size:46px}.sk .pills{margin:0}
.skc{display:flex;justify-content:space-between;align-items:center;background:var(--bg);border:2px solid var(--ink);border-radius:12px;padding:10px 16px;margin-bottom:14px}.skc b{font-size:30px;font-family:'Bricolage Grotesque',sans-serif}
.qtybig{display:flex;gap:10px;align-items:center;margin:14px 0 10px}.qtybig button{width:46px;height:46px;border:2px solid var(--ink);background:#fff;border-radius:12px;font-size:24px;font-weight:800;cursor:pointer;box-shadow:2px 2px 0 var(--ink)}.qtybig button:active{transform:translate(2px,2px);box-shadow:none}
.qtybig input{flex:1;min-width:0;height:46px;text-align:center;font:800 24px 'Bricolage Grotesque',sans-serif;border:2px solid var(--ink);border-radius:12px}
.chips{display:flex;gap:8px;flex-wrap:wrap}.chip{border:2px solid var(--ink);background:var(--yel);border-radius:99px;padding:3px 16px;font:700 14px 'DM Sans',sans-serif;cursor:pointer}
.skp{margin:14px 0;padding:10px 14px;border:2px dashed var(--ink);border-radius:12px;text-align:center}.skp b{font-size:20px}
.sw{display:flex;gap:10px;align-items:center;font-weight:700;margin-bottom:16px;cursor:pointer}.sw input{width:20px;height:20px;accent-color:var(--red)}

.pf label{display:block;font-weight:700;font-size:14px;margin:12px 0 2px}.pf h2{margin-bottom:2px}.pf .pills{margin:4px 0 0}.pf .in{margin:4px 0}
.pfprev{display:flex;gap:14px;align-items:center;border:2px solid var(--ink);border-radius:14px;padding:10px 14px;margin:12px 0;background:var(--bg)}
.pfprev .thumb{flex:none;width:72px;height:72px;font-size:40px;padding:0;border:2px solid var(--ink);border-radius:12px;display:grid;place-items:center}
.pv-Drink{background:#9edbff}.pv-Dessert{background:#ffb3c7}.pv-Meal{background:var(--yel)}
.emojis{display:flex;flex-wrap:wrap;gap:6px;margin:4px 0 8px}.emo{width:42px;height:42px;font-size:22px;border:2px solid var(--ink);border-radius:10px;background:#fff;cursor:pointer}.emo.on{background:var(--yel);box-shadow:2px 2px 0 var(--ink)}
.pill:disabled{opacity:.5;cursor:not-allowed}.pfb{margin-top:16px}
/* ---- phone layout ---- */
@media(max-width:820px){.adm{grid-template-columns:minmax(0,1fr)}.adm>*{min-width:0}.tw{max-width:100%}.split>*{min-width:0}}
@media(max-width:640px){
html{-webkit-text-size-adjust:100%}body{overflow-x:hidden}
.sec{padding:28px 14px}.hero-in{padding:34px 16px;gap:28px}.hero p{font-size:17px}.ticket{max-width:100%}
.nav{padding:10px 14px;gap:8px 10px}.logo{font-size:20px}.logo b{font-size:17px}
.nr{gap:8px}.who{font-size:13px;max-width:96px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.who small{display:none}
.nav nav{order:3;width:100%;overflow-x:auto;gap:18px;padding-bottom:2px}
.grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}
.thumb{font-size:54px;padding:14px 0}.cb{padding:12px}.cb h3{font-size:17px}.cb p{display:none}.row{flex-direction:column;align-items:stretch;gap:8px}.row .btn{width:100%}.price{font-size:18px}
.stats4{grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.st{padding:14px}.st b{font-size:22px}
.side{flex-direction:row;overflow-x:auto;gap:6px;padding:8px}.side a{white-space:nowrap;padding:8px 14px}
.bar{grid-template-columns:84px 1fr auto;gap:8px}.pad{padding:16px}
.line{flex-wrap:wrap}.lt{min-width:0;margin-left:auto}.banner{padding:26px}.cta .btn{flex:1}
.sh{flex-direction:column;align-items:stretch}.sh .in.sm{width:100%}.box{padding:20px}
.tw{overflow:visible}.tbl,.tbl tbody{display:block}.tbl tr:first-child{display:none}
.tbl tr{display:block;padding:12px 14px;border-bottom:2px dashed #d8d8d8}.tbl tr:last-child{border:0}
.tbl td{display:flex;justify-content:space-between;align-items:center;gap:12px;border:0;padding:4px 0;text-align:right}
.tbl td::before{content:attr(data-label);font-weight:700;color:var(--mut);text-align:left;flex:none}
.tbl td.em{justify-content:flex-start;font-size:34px}.tbl td.act{flex-wrap:wrap;justify-content:flex-end;gap:6px}.tbl td .in.sm{max-width:60%}
}

.inl{display:inline}.flash{max-width:1180px;margin:18px auto 0;padding:12px 18px;border:2px solid var(--ink);border-radius:12px;background:#b7f0cf;font-weight:700;box-shadow:var(--sh)}
.flash.err{background:#ffb3b0}main>.flash{width:calc(100% - 40px)}
a.pill{display:inline-block}.qtyin{width:76px;margin:0;padding:6px 8px;text-align:center}
.qf{display:flex;gap:6px;align-items:center}.line .x{background:none;border:0;cursor:pointer;font-size:17px}
.formcard{max-width:560px;margin:0 auto;padding:26px}.formcard label{display:block;font-weight:700;margin:12px 0 2px}.formcard h2{margin-bottom:6px}
.choice{display:flex;gap:20px;margin:6px 0 10px;font-weight:700}.choice label{margin:0;display:flex;gap:6px;align-items:center;cursor:pointer}.choice input{width:auto;margin:0;accent-color:var(--red)}
.chips{display:flex;gap:8px;flex-wrap:wrap;margin:6px 0 14px}.chip{border:2px solid var(--ink);background:var(--yel);border-radius:99px;padding:3px 16px;font:700 14px 'DM Sans',sans-serif;cursor:pointer}
.bigno{display:flex;justify-content:space-between;align-items:center;border:2px solid var(--ink);border-radius:12px;padding:10px 16px;background:var(--bg);margin:12px 0}.bigno b{font:800 30px 'Bricolage Grotesque',sans-serif}
.sw{display:flex;gap:10px;align-items:center;font-weight:700;margin:12px 0 16px}.sw input{width:20px;height:20px;margin:0;accent-color:var(--red)}
.two2{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.rcp{max-width:400px;margin:0 auto;padding:24px}.rcact{max-width:400px;margin:18px auto 0;text-align:center}.rcact .btn{margin:4px}
.pfx{display:flex;gap:10px;flex-wrap:wrap;align-items:end}.pfx .in{margin:0}.filters{display:flex;gap:10px;flex-wrap:wrap;margin:14px 0}
.cx .oi{text-decoration:line-through;color:var(--mut)}
@media print{
 .nav,footer,.noprint,.flash{display:none!important}
 body{background:#fff}main{padding:0}.sec{padding:0}
 .rcp{box-shadow:none;border:0;width:80mm;max-width:80mm;margin:0;padding:0}
 body.np .rcp{display:none}
 .bd{border-color:#000;background:#fff!important;color:#000!important}.stamp{color:#000;border-color:#000}.mut,.rh p{color:#000}
 @page{margin:6mm}
}
.authlogo{text-align:center;margin-bottom:18px}.bigbun{display:inline-grid;place-items:center;width:92px;height:92px;font-size:54px;background:var(--yel);border:3px solid var(--ink);border-radius:50%;box-shadow:var(--sh)}
.authlogo h1{font-size:30px;font-weight:800;margin-top:12px}a.flash{display:block;text-decoration:none;color:inherit}
""";
}
