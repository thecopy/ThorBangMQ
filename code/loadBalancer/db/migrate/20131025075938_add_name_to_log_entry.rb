class AddNameToLogEntry < ActiveRecord::Migration
  def change
    add_column :log_entries, :name, :string
  end
end
