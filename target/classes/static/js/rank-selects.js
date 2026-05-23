(function () {
    const gameSelect = document.getElementById("gameId");
    const rankSelects = Array.from(document.querySelectorAll("[data-rank-select]"));

    if (!gameSelect || rankSelects.length === 0) {
        return;
    }

    function syncRankOptions() {
        const selectedGameId = gameSelect.value;

        rankSelects.forEach((select) => {
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
        });

        syncRankRange();
    }

    function updatePreview(select) {
        const preview = document.querySelector(`[data-rank-preview-for="${select.id}"]`);
        if (!preview) {
            return;
        }

        const selectedOption = select.selectedOptions[0];
        const imageUrl = selectedOption ? selectedOption.dataset.imageUrl : "";

        if (imageUrl) {
            preview.src = imageUrl;
            preview.hidden = false;
        } else {
            preview.removeAttribute("src");
            preview.hidden = true;
        }
    }

    function syncRankRange() {
        const minSelect = document.getElementById("minRank");
        const maxSelect = document.getElementById("maxRank");

        if (!minSelect || !maxSelect) {
            return;
        }

        const minOrder = selectedSortOrder(minSelect);
        const maxOrder = selectedSortOrder(maxSelect);

        Array.from(maxSelect.options).forEach((option) => {
            if (!option.dataset.sortOrder || option.hidden) {
                return;
            }
            option.disabled = minOrder !== null && Number(option.dataset.sortOrder) < minOrder;
        });

        Array.from(minSelect.options).forEach((option) => {
            if (!option.dataset.sortOrder || option.hidden) {
                return;
            }
            option.disabled = maxOrder !== null && Number(option.dataset.sortOrder) > maxOrder;
        });

        if (maxSelect.selectedOptions[0] && maxSelect.selectedOptions[0].disabled) {
            maxSelect.value = "";
        }
        if (minSelect.selectedOptions[0] && minSelect.selectedOptions[0].disabled) {
            minSelect.value = "";
        }

        updatePreview(minSelect);
        updatePreview(maxSelect);
    }

    function selectedSortOrder(select) {
        const option = select.selectedOptions[0];
        if (!option || !option.dataset.sortOrder) {
            return null;
        }
        return Number(option.dataset.sortOrder);
    }

    gameSelect.addEventListener("change", syncRankOptions);
    rankSelects.forEach((select) => select.addEventListener("change", () => {
        updatePreview(select);
        syncRankRange();
    }));
    syncRankOptions();

    document.querySelectorAll("[data-future-datetime]").forEach((input) => {
        input.min = nextMinuteValue();
    });

    function nextMinuteValue() {
        const date = new Date();
        date.setMinutes(date.getMinutes() + 1);
        date.setSeconds(0, 0);

        const year = date.getFullYear();
        const month = pad(date.getMonth() + 1);
        const day = pad(date.getDate());
        const hours = pad(date.getHours());
        const minutes = pad(date.getMinutes());

        return `${year}-${month}-${day}T${hours}:${minutes}`;
    }

    function pad(value) {
        return String(value).padStart(2, "0");
    }
})();
