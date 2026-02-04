const VerticalPager = require('ti.verticalpager');

const win = Ti.UI.createWindow({
    backgroundColor: '#000'
});

// Array de cores para as páginas
const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'];

// Criar views iniciais
const initialViews = [];
for (let i = 0; i < 5; i++) {
    initialViews.push(createPageView(i, colors[i % colors.length]));
}

// Criar o VerticalPager
const pager = VerticalPager.createView({
    top: 0,
    left: 0,
    right: 0,
    bottom: 80,
    views: initialViews,
    cacheSize: 3,
    pageIndicator: {
        currentPageIndicatorColor: colors[0],
        pageIndicatorColor: colors[0] + '30', // Efeito vidro
        type: VerticalPager.INDICATOR_TYPE_VERTICAL,
        right: 16,
        bottom: 100
    }
});

// EVENTO: scrollstart
pager.addEventListener('scrollstart', function(e) {
    console.log('📱 SCROLLSTART - Página atual:', e.currentPage);
});

// EVENTO: scroll
pager.addEventListener('scroll', function(e) {
    console.log('📜 SCROLL - Offset:', e.offset.toFixed(2));
});

// EVENTO: scrollend
pager.addEventListener('scrollend', function(e) {
    console.log('🛑 SCROLLEND - Parou na página:', e.currentPage);
    updatePageLabel();
});

// EVENTO: change
pager.addEventListener('change', function(e) {
    console.log('🔄 CHANGE - Mudou de', e.previousPage, 'para', e.currentPage);
    updatePageLabel();
    
    // 🎨 Atualiza cores do pageIndicator (efeito vidro)
    const currentColor = colors[e.currentPage % colors.length];
    pager.pageIndicatorColor = currentColor + '30';         // Transparente
    pager.currentPageIndicatorColor = currentColor;         // Sólido
});

win.add(pager);

// ====================================
// CONTROLES NA PARTE INFERIOR
// ====================================

const controlsContainer = Ti.UI.createView({
    bottom: 0,
    height: 80,
    width: Ti.UI.FILL,
    backgroundColor: '#1a1a1a'
});

// Label mostrando página atual
const pageLabel = Ti.UI.createLabel({
    top: 5,
    text: 'Página: 0 / 4',
    color: '#fff',
    font: { fontSize: 14, fontWeight: 'bold' }
});
controlsContainer.add(pageLabel);

// Container dos botões
const buttonsContainer = Ti.UI.createView({
    bottom: 5,
    height: 50,
    layout: 'horizontal'
});

// Botão: Página Anterior
const btnPrev = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '⬆️ Prev',
    backgroundColor: '#4ECDC4',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnPrev.addEventListener('click', function() {
    const currentPage = pager.currentPage || 0;
    if (currentPage > 0) {
        pager.scrollToPage(currentPage - 1);
    }
});
buttonsContainer.add(btnPrev);

// Botão: Próxima Página
const btnNext = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '⬇️ Next',
    backgroundColor: '#4ECDC4',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnNext.addEventListener('click', function() {
    const currentPage = pager.currentPage || 0;
    const totalPages = pager.views ? pager.views.length : 0;
    if (currentPage < totalPages - 1) {
        pager.scrollToPage(currentPage + 1);
    }
});
buttonsContainer.add(btnNext);

// Botão: Ir para Página 0
const btnGoToFirst = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '🏠 First',
    backgroundColor: '#FF6B6B',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnGoToFirst.addEventListener('click', function() {
    pager.scrollToPage(0);
});
buttonsContainer.add(btnGoToFirst);

// Botão: Adicionar View
const btnAdd = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '➕ Add',
    backgroundColor: '#45B7D1',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnAdd.addEventListener('click', function() {
    const totalPages = pager.views ? pager.views.length : 0;
    const newView = createPageView(totalPages, colors[totalPages % colors.length]);
    pager.addView(newView);
    updatePageLabel();
    Ti.API.info('✅ View adicionada! Total:', totalPages + 1);
});
buttonsContainer.add(btnAdd);

// Botão: Remover Última View
const btnRemove = Ti.UI.createButton({
    left: 5,
    width: 70,
    height: 40,
    title: '➖ Remove',
    backgroundColor: '#E74C3C',
    color: '#fff',
    font: { fontSize: 12, fontWeight: 'bold' },
    borderRadius: 8
});
btnRemove.addEventListener('click', function() {
    const totalPages = pager.views ? pager.views.length : 0;
    if (totalPages > 1) {
        pager.removeView(totalPages - 1);
        updatePageLabel();
        Ti.API.info('🗑️ View removida! Total:', totalPages - 1);
    } else {
        alert('Precisa ter pelo menos 1 página!');
    }
});
buttonsContainer.add(btnRemove);

controlsContainer.add(buttonsContainer);
win.add(controlsContainer);

// ====================================
// FUNÇÕES AUXILIARES
// ====================================

function createPageView(index, backgroundColor) {
    const view = Ti.UI.createView({
        backgroundColor: backgroundColor
    });
    
    const contentView = Ti.UI.createView({
        layout: 'vertical',
        height: Ti.UI.SIZE
    });
    
    const title = Ti.UI.createLabel({
        top: 100,
        text: `PÁGINA ${index}`,
        color: '#fff',
        font: { fontSize: 48, fontWeight: 'bold' },
        textAlign: 'center'
    });
    contentView.add(title);
    
    const subtitle = Ti.UI.createLabel({
        top: 20,
        text: `Swipe para cima ou para baixo\npara navegar`,
        color: '#ffffffcc',
        font: { fontSize: 16 },
        textAlign: 'center'
    });
    contentView.add(subtitle);
    
    // Emoji grande
    const emoji = Ti.UI.createLabel({
        top: 40,
        text: getEmoji(index),
        font: { fontSize: 120 },
        textAlign: 'center'
    });
    contentView.add(emoji);
    
    // Info adicional
    const info = Ti.UI.createLabel({
        top: 40,
        text: `Esta é a view #${index}\nCache Size: 3 páginas\nMemória otimizada! 🚀`,
        color: '#ffffffaa',
        font: { fontSize: 14 },
        textAlign: 'center'
    });
    contentView.add(info);
    
    view.add(contentView);
    
    return view;
}

function getEmoji(index) {
    const emojis = ['🎬', '📱', '🎮', '🎵', '🎨', '⚡', '🔥', '💎', '🌟', '🚀'];
    return emojis[index % emojis.length];
}

function updatePageLabel() {
    const currentPage = pager.currentPage || 0;
    const totalPages = pager.views ? pager.views.length : 0;
    pageLabel.text = `Página: ${currentPage} / ${totalPages - 1}`;
}

win.open();