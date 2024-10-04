// Run when DOM is fully loaded
window.onload = () => {
    trimCode();
};

// Trim whitespaces around code blocks.
function trimCode(){
    // Select all <code> elements inside <pre> blocks
    const codeBlocks = document.querySelectorAll('pre code');

    // Loop through each code block and trim the content
    codeBlocks.forEach((block) => {
        block.textContent = block.textContent.trim();
    });

    // Re-apply Prism syntax highlighting
    Prism.highlightAll();
}