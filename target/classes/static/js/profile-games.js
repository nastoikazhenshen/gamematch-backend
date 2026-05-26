(function () {
    const rankSelects = Array.from(document.querySelectorAll("[data-profile-rank-select]"));

    function gameIdFor(select) {
        if (select.dataset.profileFixedGameId) {
            return select.dataset.profileFixedGameId;
        }

        const sourceId = select.dataset.profileGameSource;
        const gameSelect = sourceId ? document.getElementById(sourceId) : null;
        return gameSelect ? gameSelect.value : "";
    }

    function syncSelect(select) {
        const selectedGameId = gameIdFor(select);
        const selectedOption = select.selectedOptions[0];

        Array.from(select.options).forEach((option) => {
            if (!option.dataset.gameId) {
                option.hidden = false;
                return;
            }

            option.hidden = Boolean(selectedGameId) && option.dataset.gameId !== selectedGameId;
        });

        if (selectedOption && selectedOption.hidden) {
            select.value = "";
        }

        updatePreview(select);
    }

    function updatePreview(select) {
        const preview = document.querySelector(`[data-profile-rank-preview-for="${select.id}"]`);
        if (!preview) {
            return;
        }

        const option = select.selectedOptions[0];
        const imageUrl = option ? option.dataset.imageUrl : "";
        if (imageUrl) {
            preview.src = imageUrl;
            preview.hidden = false;
            return;
        }

        preview.removeAttribute("src");
        preview.hidden = true;
    }

    rankSelects.forEach((select) => {
        const sourceId = select.dataset.profileGameSource;
        const gameSelect = sourceId ? document.getElementById(sourceId) : null;

        select.addEventListener("change", () => updatePreview(select));
        if (gameSelect) {
            gameSelect.addEventListener("change", () => syncSelect(select));
        }

        syncSelect(select);
    });
})();
