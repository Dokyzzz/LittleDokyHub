// ── Active tab on scroll ──
const sections = document.querySelectorAll('section[id]');
const tabs = document.querySelectorAll('.cat-tab');

window.addEventListener('scroll', () => {
    let current = '';
    sections.forEach(section => {
        const sectionTop = section.offsetTop - 200;
        if (window.scrollY >= sectionTop) {
            current = section.getAttribute('id');
        }
    });

    tabs.forEach(tab => {
        tab.classList.remove('active');
        if (tab.getAttribute('href') === '#' + current) {
            tab.classList.add('active');
        }
    });
});

// ── Smooth scroll for tabs ──
tabs.forEach(tab => {
    tab.addEventListener('click', (e) => {
        e.preventDefault();
        const target = document.querySelector(tab.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });
});