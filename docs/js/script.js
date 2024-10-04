// Run when DOM is fully loaded
window.onload = () => {
    relPath = getRelPath();
    console.log(relPath)
    renderTemplates(relPath);
};

// Function to get relative path (used to find assets)
function getRelPath(){
    const basePath = window.location.pathname;
    const depth = window.location.pathname.split('/').length - 3;
    return './' + '../'.repeat(depth);
}

// Function to toggle menu (mobile view)
function toggleMenu() {
    const navLinks = document.querySelector('.nav-links');
    navLinks.classList.toggle('active');
}

// Function to load templates using fetch
async function loadTemplate(templateUrl) {
    const response = await fetch(templateUrl);
    return response.text();
}

// Function to render the templates with Mustache
async function renderTemplates() {
    // Load and render header + nav
    const headerTemplate = await loadTemplate(`${relPath}templates/header.mustache`);
    const headerData = { relPath };
    const renderedHeader = Mustache.render(headerTemplate, headerData);
    document.getElementById('header').innerHTML = renderedHeader;

    // Load and render footer
    const footerTemplate = await loadTemplate(`${relPath}/templates/footer.mustache`);
    const renderedFooter = Mustache.render(footerTemplate, {});
    document.getElementById('footer').innerHTML = renderedFooter;
}

